## 🔐 Cookie-Based Authentication Flow

1. ### Registration or Login (Frontend → Backend)

User submits form → Angular sends POST /auth/login with credentials
                  → Backend validates username/password

2. ### Backend Creates JWT & Sets Cookie
When authentication succeeds, the backend:

- Generates a JWT token containing username and expiration
- Creates an httpOnly cookie named DEVCONNECT_JWT
- Sends cookie in response header: Set-Cookie: DEVCONNECT_JWT=<token>; Path=/; HttpOnly; SameSite=Lax


### Key Cookie Properties:

- httpOnly=true → JavaScript cannot access it (XSS protection)
- secure=false (dev only) → Would be true in production (HTTPS only)
- SameSite=Lax → Protects against CSRF attacks
path=/ → Cookie sent with all requests to your domain

3. ### Browser Automatically Stores Cookie
The browser saves the cookie. Your Angular code never sees or stores the token—the browser handles it automatically.

4. ### Authenticated Requests (Automatic)
When Angular makes any API request (e.g., GET /api/users/my-profile):

Frontend: authService.getMyProfile() with { withCredentials: true }
         ↓
Browser automatically attaches: Cookie: DEVCONNECT_JWT=<token>
         ↓
Backend: JwtFilter intercepts request
         ↓
JwtFilter checks: 1. Cookies first (finds DEVCONNECT_JWT)
                  2. Authorization header (fallback)
         ↓
Validates JWT → Extracts username → Sets SecurityContext
         ↓
Controller method executes with authenticated user

5. ### How JwtFilter Extracts the Cookie
JwtFilter.java:
```java
private String resolveToken(HttpServletRequest request) {
    // Try cookies first (preferred for web apps)
    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
        for (Cookie cookie : cookies) {
            if ("DEVCONNECT_JWT".equals(cookie.getName())) {
                return cookie.getValue(); // Found it!
            }
        }
    }
    
    // Fallback: check Authorization header (for mobile/API clients)
    String bearerToken = request.getHeader("Authorization");
    if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
        return bearerToken.substring(7);
    }
    
    return null; // No token found
}
```

6. Logout Process

User clicks logout → authService.logout()
                   ↓
Backend receives POST /auth/logout
                   ↓
Backend sends cookie with maxAge=0 (deletes it)
                   ↓
Browser removes DEVCONNECT_JWT cookie
                   ↓
All future requests are unauthenticated

7. Why withCredentials: true is Critical
In auth.service.ts, every request has:

```js
return this.http.post<void>(`${API_BASE}/auth/login`, req, { 
  withCredentials: true // ← REQUIRED!
});
```

Without this:

Browser won't send cookies with cross-origin requests
Backend won't receive DEVCONNECT_JWT cookie
Authentication fails

## 🎯 Key Advantages of Cookie-Based Auth

| Advantage              | Explanation                                                     |
|------------------------|-----------------------------------------------------------------|
| XSS Protection         | JavaScript cannot access httpOnly cookies                       |
| No Storage Code        | Browser handles cookie storage automatically                    |
| CSRF Protection        | SameSite=Lax prevents cross-site requests                       |
| Automatic Inclusion    | Cookies sent with every request — no manual headers             |
| Simple Logout          | Just expire the cookie, no frontend cleanup                     |


## 🔄 Complete Authentication Sequence Diagram

┌──────────┐           ┌──────────┐           ┌──────────┐
│ Browser  │           │ Angular  │           │ Backend  │
└────┬─────┘           └────┬─────┘           └────┬─────┘
     │ 1. User enters       │                      │
     │    username/pwd      │                      │
     ├─────────────────────>│                      │
     │                      │ 2. POST /auth/login  │
     │                      │  { username, pwd }   │
     │                      │  withCredentials:true│
     │                      ├─────────────────────>│
     │                      │                      │ 3. Validate
     │                      │                      │    credentials
     │                      │                      │
     │                      │  4. Set-Cookie:      │
     │                      │     DEVCONNECT_JWT   │
     │                      │     HttpOnly         │
     │<─────────────────────┴──────────────────────┤
     │ 5. Browser stores                           │
     │    cookie                                   │
     │    automatically                            │
     │                      │                      │
     │ 6. Navigate to /home │                      │
     ├─────────────────────>│                      │
     │                      │ 7. GET /api/users/   │
     │                      │    my-profile        │
     │                      │    Cookie: DEVCONN...│
     │<─────────────────────┴─────────────────────>│
     │                      │                      │ 8. JwtFilter
     │                      │                      │    extracts from
     │                      │                      │    cookies
     │                      │                      │
     │                      │  9. User profile     │
     │                      │<─────────────────────┤
     │ 10. Display home     │                      │
     │     page             │                      │