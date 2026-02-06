# Running DevConnect Full Stack

This guide helps you run both the backend (Spring Boot) and frontend (Angular) services for local development and testing.

## Prerequisites

- Java 17 or higher
- Node.js 18+ and npm
- PostgreSQL database running on `localhost:5432`
- Database named `dev_connect`

## Quick Start

### 1. Start the Backend (Spring Boot)

```bash
cd dev_connect_backend
./gradlew bootRun
```

The backend will start on **http://localhost:8080**

### 2. Start the Frontend (Angular)

In a new terminal:

```bash
cd dev_connect_frontend
npm install  # First time only
npm start
```

The frontend will start on **http://localhost:4200**

The Angular dev server is configured with a proxy that forwards:

- `/api/*` requests to `http://localhost:8080`
- `/auth/*` requests to `http://localhost:8080`

## Testing the Full Stack

1. Open your browser to **http://localhost:4200**
2. You should see the login page
3. Click "Register here" to create a new account
4. Fill in the registration form with:
   - Username (min 3 characters)
   - Email
   - Password (min 8 characters)
   - Confirm Password
   - Skills (comma-separated, e.g., "Java, Spring Boot, Angular")
   - Bio (optional)
5. After successful registration, you'll be automatically logged in with an httpOnly cookie
6. Try logging out and logging back in

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│  Browser (http://localhost:4200)                            │
└────────────────────┬────────────────────────────────────────┘
                     │
                     │ HTTP Requests
                     │
┌────────────────────▼────────────────────────────────────────┐
│  Angular Dev Server (Port 4200)                             │
│  - Serves Angular app                                       │
│  - Proxies /api/* and /auth/* to backend                    │
└────────────────────┬────────────────────────────────────────┘
                     │
                     │ Proxied Requests
                     │
┌────────────────────▼────────────────────────────────────────┐
│  Spring Boot Backend (Port 8080)                            │
│  - REST API endpoints                                       │
│  - JWT authentication with httpOnly cookies                 │
│  - CORS enabled for http://localhost:4200                   │
└────────────────────┬────────────────────────────────────────┘
                     │
                     │ JDBC
                     │
┌────────────────────▼────────────────────────────────────────┐
│  PostgreSQL Database (Port 5432)                            │
│  Database: dev_connect                                      │
└─────────────────────────────────────────────────────────────┘
```

## Configuration Details

### Backend (Spring Boot)

- **Port**: 8080
- **Database**: PostgreSQL at `localhost:5432/dev_connect`
- **CORS**: Allows `http://localhost:4200` with credentials
- **Cookie**: `DEVCONNECT_JWT` (httpOnly, SameSite=Lax, path=/)

### Frontend (Angular)

- **Port**: 4200
- **Proxy**: Configured in `proxy.conf.json`
- **API Base**: All requests to `/api/*` and `/auth/*` are proxied to backend
- **Credentials**: All HTTP requests include `withCredentials: true` for cookie support

## API Endpoints

### Public Endpoints (No Authentication)

- `POST /api/users/register` - Register new user
- `POST /auth/login` - Login (returns httpOnly cookie)
- `POST /auth/logout` - Logout (clears cookie)
- `GET /api/users?username=X` - Get public profile
- `GET /api/posts/public` - List public posts
- `GET /api/search?keyword=X` - Search posts

### Protected Endpoints (Requires Authentication)

- `GET /api/users/my-profile` - Get current user's profile
- `PUT /api/users/my-profile` - Update current user's profile
- `POST /api/posts` - Create post
- `PUT /api/posts/{id}` - Update post
- `DELETE /api/posts/{id}` - Delete post

## Troubleshooting

### Backend won't start

- Check PostgreSQL is running: `psql -U rekhakumari -d dev_connect`
- Verify database credentials in `application.properties`
- Check port 8080 is not in use: `lsof -i :8080`

### Frontend won't start

- Run `npm install` in the frontend directory
- Check port 4200 is not in use: `lsof -i :4200`
- Clear node_modules and reinstall if needed

### CORS errors

- Ensure backend is running on port 8080
- Verify CORS configuration in `SecurityConfig.java` includes `http://localhost:4200`
- Check browser console for specific CORS error messages

### Cookie not being set

- Check browser dev tools > Application > Cookies
- Verify the cookie name is `DEVCONNECT_JWT`
- Ensure `withCredentials: true` is set in Angular HTTP calls
- Check backend logs for cookie creation

### Proxy not working

- Verify `proxy.conf.json` exists in frontend root
- Check `angular.json` has `"proxyConfig": "proxy.conf.json"` in serve options
- Restart Angular dev server after proxy config changes
- Check terminal logs for proxy debug messages

## Development Workflow

1. **Backend changes**: Edit Java files, Spring Boot auto-reloads
2. **Frontend changes**: Edit TypeScript/HTML/CSS, Angular auto-reloads
3. **Database changes**: Update entities, Hibernate DDL auto-updates (dev mode)
4. **API changes**: Update both backend controller and frontend service

## Running Tests

### Backend Tests

```bash
cd dev_connect_backend
./gradlew test
```

### Frontend Tests

```bash
cd dev_connect_frontend
npm test
```

## Next Steps

- Implement Phase 4: Frontend session handling (AuthStateService, AuthGuard)
- Add navbar with user profile and logout
- Implement profile viewing and editing
- Add post creation and listing features