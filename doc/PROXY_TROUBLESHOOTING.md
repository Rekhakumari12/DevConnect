# Angular Dev Server Proxy Troubleshooting Guide

## Overview

The DevConnect frontend uses Angular CLI's proxy feature to forward API requests from `http://localhost:4200` to the backend at `http://localhost:8080`. This document explains the setup and how to troubleshoot common issues.

---

## Proxy Configuration

### Files Involved

1. **`dev_connect_frontend/proxy.conf.json`** - Proxy rules configuration
2. **`dev_connect_frontend/angular.json`** - Angular CLI configuration that references the proxy

### Proxy Rules

```json
{
  "/api/*": {
    "target": "http://localhost:8080",
    "secure": false,
    "logLevel": "debug",
    "changeOrigin": true
  },
  "/auth/*": {
    "target": "http://localhost:8080",
    "secure": false,
    "logLevel": "debug",
    "changeOrigin": true
  }
}
```

**What this does:**

- Any request to `http://localhost:4200/api/*` → forwarded to `http://localhost:8080/api/*`
- Any request to `http://localhost:4200/auth/*` → forwarded to `http://localhost:8080/auth/*`
- `changeOrigin: true` - Changes the origin header to match the target
- `logLevel: debug` - Shows proxy activity in the terminal

### Angular CLI Configuration

In `angular.json`, the proxy is referenced in the serve options:

```json
"serve": {
  "builder": "@angular/build:dev-server",
  "options": {
    "proxyConfig": "proxy.conf.json"
  }
}
```

---

## Quick Start (If Everything Fails)

**Complete reset procedure:**

```bash
# 1. Stop any running dev servers (Ctrl+C in all terminals)

# 2. Verify backend is running
cd /Users/rekhakumari/Documents/DevConnect/dev_connect_backend
docker compose ps
# Should show postgres_db and spring_app as "Up"

# 3. If backend not running, start it
docker compose up -d

# 4. Navigate to frontend directory (CRITICAL!)
cd /Users/rekhakumari/Documents/DevConnect/dev_connect_frontend

# 5. Verify you're in the right place
pwd
# Must show: /Users/rekhakumari/Documents/DevConnect/dev_connect_frontend

ls proxy.conf.json
# Must exist and show: proxy.conf.json

# 6. Start Angular dev server
npm start

# 7. Wait for this message:
# ** Angular Live Development Server is listening on localhost:4200 **

# 8. In a new terminal, test the proxy:
curl -X POST http://localhost:4200/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "quicktest",
    "email": "quick@test.com",
    "password": "123456",
    "skills": ["JavaScript"],
    "bio": "Quick test"
  }'

# Should return HTTP 201 with user data
```

---

## Common Issues & Solutions

### ❌ Issue 0: Dev Server Not Running At All

**Symptom:**

```bash
curl: (7) Failed to connect to localhost port 4200: Connection refused
```

**Cause:** Angular dev server is not started.

**Solution:**

```bash
# IMPORTANT: Must be in the frontend directory!
cd /Users/rekhakumari/Documents/DevConnect/dev_connect_frontend

# Verify you're in the right directory
pwd  # Should show: /Users/rekhakumari/Documents/DevConnect/dev_connect_frontend
ls package.json  # Should exist

# Start the dev server
npm start

# Server should start and show:
# ** Angular Live Development Server is listening on localhost:4200 **
```

**Common mistake:** Running `npm start` from the parent `DevConnect` directory instead of `dev_connect_frontend`.

---

### ❌ Issue 1: Getting 404 errors on `/api/*` requests

**Symptom:**

```bash
GET http://localhost:4200/api/users/my-profile 404 (Not Found)
POST http://localhost:4200/api/users/register 404 (Not Found)
```

**Cause:** The Angular dev server was started **before** the proxy configuration was added or modified.

**Solution:**

1. Stop the Angular dev server (Ctrl+C in the terminal)
2. Restart it:
   ```bash
   cd dev_connect_frontend
   npm start
   ```

**Why:** Angular CLI only reads `proxy.conf.json` when the dev server starts. Changes to the proxy config require a restart.

---

### ❌ Issue 2: Backend not responding

**Symptom:**

```bash
GET http://localhost:4200/api/users/my-profile net::ERR_CONNECTION_REFUSED
```

**Cause:** Backend is not running.

**Solution:**

1. Verify backend is running:

   ```bash
   cd dev_connect_backend
   docker compose ps
   ```

   You should see both `postgres_db` and `spring_app` with status "Up"

2. If not running, start it:

   ```bash
   docker compose up --build -d
   ```

3. Test backend directly:
   ```bash
   curl http://localhost:8080/api/users/my-profile
   # Should return 401 (expected without auth)
   ```

---

### ❌ Issue 3: CORS errors even with proxy

**Symptom:**

```
Access to XMLHttpRequest at 'http://localhost:8080/api/...' from origin 'http://localhost:4200'
has been blocked by CORS policy
```

**Cause:** You're accessing the backend directly (port 8080) instead of going through the proxy (port 4200).

**Solution:**
Ensure all API calls in Angular services use relative URLs (no `http://localhost:8080`):

✅ **Correct:**

```typescript
this.http.post('/api/users/register', data);
```

❌ **Incorrect:**

```typescript
this.http.post('http://localhost:8080/api/users/register', data);
```

The proxy only works when you use relative URLs. The Angular dev server intercepts these and forwards them.

---

### ❌ Issue 4: Proxy not logging activity

**Symptom:** No proxy logs appear in the terminal even though requests are being made.

**Cause:** `logLevel` might be set too high or proxy isn't being used.

**Solution:**

1. Verify `logLevel: "debug"` in `proxy.conf.json`
2. Restart the dev server
3. Check the **terminal where `npm start` is running** - proxy logs appear there, not in the browser console

**Expected output:**

```
[HPM] POST /api/users/register -> http://localhost:8080
[HPM] GET /api/users/my-profile -> http://localhost:8080
```

---

### ❌ Issue 5: Cookies not being set/sent

**Symptom:** Login succeeds but subsequent requests fail with 401.

**Cause:** Missing `withCredentials: true` in HTTP requests.

**Solution:**
All authentication-related HTTP calls must include `{ withCredentials: true }`:

```typescript
// AuthService
login(payload: LoginRequest): Observable<any> {
  return this.http.post('/auth/login', payload, {
    withCredentials: true  // ← Required for cookies
  });
}

getMyProfile(): Observable<any> {
  return this.http.get('/api/users/my-profile', {
    withCredentials: true  // ← Required for cookies
  });
}
```

**Why:** Cookies (like `DEVCONNECT_JWT`) are only sent across domains if `withCredentials: true` is set, even when using the proxy.

---

## Verification Steps

### ✅ Step 1: Verify Backend is Running

```bash
curl http://localhost:8080/api/users/my-profile
```

**Expected:** HTTP 401 (Unauthorized) - This is correct! It means backend is running and rejecting unauthenticated requests.

**Not Expected:** Connection refused, timeout, or 404.

---

### ✅ Step 2: Verify Proxy Configuration

Check that `dev_connect_frontend/proxy.conf.json` exists and contains the correct rules.

Check that `dev_connect_frontend/angular.json` references it:

```bash
cd dev_connect_frontend
grep -A 2 '"serve"' angular.json | grep proxyConfig
```

**Expected output:**

```
"proxyConfig": "proxy.conf.json"
```

---

### ✅ Step 3: Verify Angular Dev Server is Using Proxy

1. Start the dev server:

   ```bash
   cd dev_connect_frontend
   npm start
   ```

2. Look for this line in the startup output:

   ```
   ** Angular Live Development Server is listening on localhost:4200 **
   ```

3. Open browser DevTools → Network tab
4. Navigate to `http://localhost:4200/register`
5. Fill in the registration form and submit
6. In Network tab, check the request to `/api/users/register`:
   - **Request URL should be:** `http://localhost:4200/api/users/register` (NOT 8080)
   - **Status should be:** 201 (Created) or 409 (Conflict if duplicate)

7. In the **terminal running `npm start`**, you should see:
   ```
   [HPM] POST /api/users/register -> http://localhost:8080
   ```

---

### ✅ Step 4: Test Registration End-to-End

```bash
# Open browser to registration page
open http://localhost:4200/register

# Or test with curl through the proxy:
curl -X POST http://localhost:4200/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "123456",
    "skills": ["Java"],
    "bio": "Test user"
  }'
```

**Expected:** HTTP 201 with user profile in response, plus a `Set-Cookie` header with `DEVCONNECT_JWT`.

---

## Quick Checklist

When things aren't working, check these in order:

- [ ] Backend is running (`docker compose ps` shows both containers "Up")
- [ ] Backend responds directly (`curl http://localhost:8080/api/users/my-profile` returns 401)
- [ ] `proxy.conf.json` exists in `dev_connect_frontend/`
- [ ] `angular.json` has `"proxyConfig": "proxy.conf.json"` in serve options
- [ ] Angular dev server was **restarted after proxy config changes**
- [ ] Browser is accessing `http://localhost:4200` (NOT `http://localhost:8080`)
- [ ] API calls use relative URLs (`/api/*`, not `http://localhost:8080/api/*`)
- [ ] All auth requests include `{ withCredentials: true }`
- [ ] No CORS errors in browser console
- [ ] Proxy logs appear in terminal running `npm start`

---

## Advanced Debugging

### Enable Verbose Proxy Logging

Modify `proxy.conf.json`:

```json
{
  "/api/*": {
    "target": "http://localhost:8080",
    "secure": false,
    "logLevel": "debug",
    "changeOrigin": true,
    "onProxyReq": "function(proxyReq, req, res) { console.log('[PROXY] Request:', req.method, req.url); }",
    "onProxyRes": "function(proxyRes, req, res) { console.log('[PROXY] Response:', proxyRes.statusCode); }"
  }
}
```

**Note:** Restart dev server after changes.

---

### Check What Port Frontend is Using

```bash
lsof -i :4200
```

**Expected:** Should show `node` process listening on port 4200.

If nothing, the dev server isn't running. If something else, there's a port conflict.

---

### Test Backend Registration Directly

Bypass the proxy entirely to verify backend is working:

```bash
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "directtest",
    "email": "direct@test.com",
    "password": "123456",
    "skills": ["Spring Boot"],
    "bio": "Direct backend test"
  }' \
  -v
```

**Expected:** HTTP 201 with JSON response and `Set-Cookie` header.

If this fails, the issue is with the backend, not the proxy.

---

## Key Concepts

### Why Use a Proxy?

1. **CORS Avoidance:** Browsers block requests from `http://localhost:4200` (frontend) to `http://localhost:8080` (backend) due to Same-Origin Policy. The proxy makes the browser think everything is on port 4200.

2. **Cookie Handling:** Cookies set by the backend (`DEVCONNECT_JWT`) work seamlessly because the browser sees all traffic as coming from `localhost:4200`.

3. **Development Only:** This proxy setup is **only for local development**. In production, you'd deploy the Angular app and backend together (same domain) or configure CORS properly.

### How the Proxy Works

```
Browser                Angular Dev Server (4200)        Spring Boot Backend (8080)
   |                            |                                |
   |-- GET /api/users/my-profile -->                            |
   |                            |                                |
   |                            |-- GET /api/users/my-profile -->|
   |                            |                                |
   |                            |<-- 401 Unauthorized ------------|
   |                            |    + headers                    |
   |<-- 401 Unauthorized -------|                                |
       + headers
```

The Angular dev server acts as a **reverse proxy**, forwarding requests and responses transparently.

---

## When to Restart the Dev Server

**Always restart after changing:**

- `proxy.conf.json` (proxy rules)
- `angular.json` (proxy configuration reference)
- Environment variables
- Any CLI configuration

**No restart needed for:**

- Component code changes (hot reload)
- Service code changes (hot reload)
- Template/CSS changes (hot reload)

---

## Workaround: Direct Backend URLs (When Proxy Fails)

### The Problem

In some cases, despite correct configuration files (`proxy.conf.json` and `angular.json`), the Angular CLI dev server fails to load the proxy middleware. Symptoms include:

- **404 errors** on API requests: `POST http://localhost:4200/api/users/register 404 (Not Found)`
- **HTML error responses** instead of JSON (e.g., "Cannot POST /api/users/register")
- **No proxy logs** in the terminal despite `logLevel: "debug"`
- Backend works perfectly when tested directly (returns 201), but fails through Angular dev server

### Debugging Steps We Followed

#### Step 1: Verify Proxy Configuration Files

```bash
# Check proxy.conf.json exists and is valid
cat dev_connect_frontend/proxy.conf.json

# Check angular.json references it
cat angular.json | grep -A 5 '"serve"'
```

**Result:** Both files were correct.

#### Step 2: Test Backend Directly

```bash
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{"username":"directtest456","email":"directtest456@example.com","password":"password123","skills":["Java"],"bio":"Test"}' \
  -i
```

**Result:** Backend returned 201 with Set-Cookie header. Backend is working perfectly.

#### Step 3: Try Explicit Proxy Flag

```bash
npx ng serve --proxy-config proxy.conf.json --port 4200
```

**Result:** Server started successfully, but proxy middleware still not forwarding requests.

#### Step 4: Check for Proxy Logs

When proxy is working, you should see logs like:

```
[HPM] POST /api/users/register -> http://localhost:8080
```

**Result:** No proxy logs appeared, confirming middleware wasn't loaded.

### The Solution: Use Direct Backend URLs

Since the proxy wasn't working despite all correct configurations, we implemented a **temporary workaround** by configuring the frontend to call the backend directly.

#### Changes Made to `auth.service.ts`

```typescript
@Injectable({ providedIn: 'root' })
export class AuthService {
  // Temporary: Use direct backend URL until proxy is fixed
  private readonly API_BASE = 'http://localhost:8080/api';
  private readonly AUTH_BASE = 'http://localhost:8080/auth';

  constructor(private readonly http: HttpClient) {}

  register(payload: RegisterRequest): Observable<any> {
    return this.http.post(`${this.API_BASE}/users/register`, payload, {
      withCredentials: true,
    });
  }

  login(payload: LoginRequest): Observable<any> {
    return this.http.post(`${this.AUTH_BASE}/login`, payload, {
      withCredentials: true,
    });
  }

  logout(): Observable<void> {
    return this.http.post<void>(
      `${this.AUTH_BASE}/logout`,
      {},
      { withCredentials: true },
    );
  }

  getMyProfile(): Observable<any> {
    return this.http.get(`${this.API_BASE}/users/my-profile`, {
      withCredentials: true,
    });
  }
}
```

#### Prerequisites for This Workaround

**1. Backend Must Have CORS Configured**

Verify in `SecurityConfig.java`:

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200"));
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}
```

**2. Start Dev Server Without Proxy Flag**

```bash
cd dev_connect_frontend
npx ng serve --port 4200
```

No need for `--proxy-config` flag since we're bypassing the proxy.

#### Verification

After making these changes:

1. Restart the Angular dev server
2. Open http://localhost:4200/register
3. Fill out the registration form
4. Submit

**Expected:** Registration succeeds, you're logged in, and redirected to /home.

### When to Use This Workaround

✅ **Use this approach when:**

- Proxy configuration is correct but not working
- Backend is confirmed working (direct curl tests pass)
- No proxy logs appear despite `logLevel: "debug"`
- You need to unblock development quickly

❌ **Don't use this for:**

- Production deployments
- When the proxy is working correctly
- When backend doesn't have CORS configured for `http://localhost:4200`

### Reverting to Proxy Later

When the proxy issue is resolved, revert `auth.service.ts`:

```typescript
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly API_BASE = '/api';
  private readonly AUTH_BASE = '/auth'; // Note: no 'http://localhost:8080'

  // ... rest of the code stays the same
}
```

Then start dev server with proxy:

```bash
npx ng serve --proxy-config proxy.conf.json --port 4200
```

---

## Related Documentation

- [Angular CLI Proxy Documentation](https://angular.io/guide/build#proxying-to-a-backend-server)
- [RUNNING_FULLSTACK.md](./RUNNING_FULLSTACK.md) - Complete setup guide
- [cookie-based-authentication-flow.md](./cookie-based-authentication-flow.md) - Authentication details

---

## Getting Help

If you've followed all troubleshooting steps and still have issues:

1. **Check browser DevTools Console** - Look for error messages
2. **Check browser DevTools Network tab** - Inspect the failing request
3. **Check terminal running `npm start`** - Look for proxy logs and errors
4. **Check Docker logs**: `docker compose logs app` - Backend errors
5. **Verify all services are running**: Backend containers + Angular dev server

**Common mistake:** Forgetting to restart the Angular dev server after adding/modifying the proxy config. **Always restart!**

**If proxy completely fails:** Use the direct backend URL workaround documented above (requires backend CORS configuration).
