# DevConnect
Goal:  Build a mini collaboration platform for developers to share project ideas, comment, and react to others’ posts — similar to a lightweight internal version of Dev.to or GitHub Discussions.

-------------------------
TECH STACK
-------------------------
Backend: Spring Boot  
Build Tool: Gradle  
Database: PostgreSQL  
ORM: Spring Data JPA / Hibernate  
Authentication: JWT  
API Style: REST 

-------------------------
SETUP INSTRUCTIONS
-------------------------
Clone the repository.

```bash
git clone https://github.com/rekhakumari12/DevConnect.git
```
Move into the project directory.
```bash
cd dev-connect
```
Build the project using Gradle.
```bash
./gradlew build
```
Run the Spring Boot application.
```bash
./gradlew bootRun
```

The server will start on http://localhost:8080.

-------------------------
API Endpoints
-------------------------

For user management, these endpoints handle registration, login, profile access, and updates. Registration allows a new user to sign up with username, email, password, and skills. The password is hashed using BCrypt before saving. Login verifies credentials and returns a JWT token. Once authenticated, the user can fetch their own profile and update basic information like skills or bio. Every endpoint except register and login is protected by JWT validation.

### User management APIs
```bash
POST   /api/auth/register
POST   /api/auth/login
GET    /api/users/me
PUT    /api/users/me
```
For project ideas (posts), these endpoints allow authenticated users to create and manage project ideas. A post contains title, description, techStack, and visibility (PUBLIC or PRIVATE). Any user can view all public posts. A logged-in user can view all their own posts, including private ones. Only the owner of a post can update or delete it. Each post automatically stores createdAt and updatedAt timestamps.

### Post management APIs
```bash
POST   /api/posts
GET    /api/posts/public
GET    /api/posts/me
PUT    /api/posts/{postId}
DELETE /api/posts/{postId}
```
For comments, users can comment only on public posts. Each comment is linked to both the post and the user who created it. Users are allowed to delete only their own comments. Fetching comments for a post is usually public since the post itself is public.

### Comment APIs
```bash
POST   /api/posts/{postId}/comments
GET    /api/posts/{postId}/comments
DELETE /api/comments/{commentId}
```
For reactions (like, support, funny, celebrate), each user can react only once per post. If the user reacts again, the existing reaction is updated instead of creating a new one. When fetching posts, the response includes total reaction count and the current user’s reaction status so the frontend knows what to display.

### Reaction APIs
```bash
POST /api/posts/{postId}/reactions
POST /api/comments/{commentId}/reactions
GET /api/posts/{postId}/reactions
GET /api/comments/{commentId}/reactions
```
For search and filtering, users can search only public posts. Searching supports filtering by tech stack keyword or partial title match. Results are paginated using Spring’s Pageable support, with a default page size of 10. Page number and size can be overridden using query parameters.

### Search APIs
```bash
GET /api/posts/search?techStack=java
GET /api/posts/search?title=spring&page=0&size=10
```
For error handling and validation, all invalid input (missing fields, invalid email, unauthorized access, forbidden actions) returns structured JSON errors. This is handled globally using @ControllerAdvice, so responses are consistent across the application.
```bash
{
  "timestamp": "2026-01-17T10:30:00",
  "status": 400,
  "error": "Validation Error",
  "message": "Email must be valid",
}
```
