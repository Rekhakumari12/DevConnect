# DevConnect Microservice — Business Domain Context

This document gives a concise, practical overview of the DevConnect backend microservice and its business context, so a new team member can quickly understand what the service does, how it’s used, and where it fits within the broader system.

## Purpose & Scope

- Connect developers by enabling user profiles, posts, comments, and lightweight reactions.
- Provide JWT-based authentication and profile management.
- Expose public read endpoints (profiles, posts, reactions, comments) while enforcing ownership for mutations.
- Persist data in PostgreSQL; no external API calls or messaging observed in code.

Additionally:

- Admin users are part of the business model (planned), primarily for moderation and featured-post curation.

## Project Objectives (from PRD)

- Introduce Spring Boot, REST design, JPA/Hibernate, and JWT security through a practical collaboration platform.
- Reinforce layering: Entity → Repository → Service → Controller with DTO mapping and validation/exception handling.
- Provide an optional Angular/React frontend to consume the APIs and manage auth tokens.
- Use developer-friendly tooling: PostgreSQL (optionally via Docker Compose), Postman/cURL, and GitHub.

## Core Capabilities

- Authentication: Login issues JWTs for stateless auth.
- Profiles: Register, view public profiles, update own profile.
- Posts: Create, update, delete own posts; public list and user-specific listing.
- Comments: Add comments to public posts; list comments; delete own comment.
- Reactions: React to posts/comments (LIKE, SUPPORT, FUNNY, CELEBRATE); toggle behavior on duplicate reaction.
- Search: Keyword search over public posts (title and tags).

## Users & Roles

- Regular Users: Create/manage profiles, posts, comments, and reactions.
- Admins (planned): Moderate content, make curation decisions such as promoting featured posts, and enforce policies.
  - Current implementation uses a single user role; admin capabilities are a future extension mapped to policy and curation needs.

## Domain Model (Entities)

- User: `id`, `username` (unique), `email` (unique), `password` (BCrypt), `skills` (list), `bio`.
- Post: `id`, `title`, `content`, `tags` (list of strings), `visibility` (`PUBLIC`/`PRIVATE`), timestamps, owner `User`.
- Comment: `id`, `content`, owner `User`, parent `Post`, `createdAt`, reactions.
- Reaction: `id`, `type` (`LIKE`, `SUPPORT`, `FUNNY`, `CELEBRATE`), target (`Post` or `Comment`), actor `User`.

Relevant code:

- User: [dev_connect_backend/src/main/java/com/example/demo/entity/User.java](dev_connect_backend/src/main/java/com/example/demo/entity/User.java)
- Post: [dev_connect_backend/src/main/java/com/example/demo/entity/Post.java](dev_connect_backend/src/main/java/com/example/demo/entity/Post.java)
- Comment: [dev_connect_backend/src/main/java/com/example/demo/entity/Comment.java](dev_connect_backend/src/main/java/com/example/demo/entity/Comment.java)
- Reaction: [dev_connect_backend/src/main/java/com/example/demo/entity/Reaction.java](dev_connect_backend/src/main/java/com/example/demo/entity/Reaction.java)
- Enums: [dev_connect_backend/src/main/java/com/example/demo/enums](dev_connect_backend/src/main/java/com/example/demo/enums)

## Business Rules & Nuances

- Ownership enforcement: Only a post’s owner can update/delete that post; only a comment’s owner can delete that comment. See `AuthUtil.verifyUserAccess(...)`.
- Visibility: Comments and reactions are only allowed on `PUBLIC` posts; private posts are protected from reads that would reveal engagement.
- Private post semantics (business intent): Used for drafts.
  - Note: Current behavior treats `PRIVATE` as non-readable (no comments/reactions), which aligns with drafts.
- Reaction toggling: Posting the same reaction type twice removes the prior reaction (toggle off); switching to a different type updates it.
- Reactions as outcomes: Reactions are intended to drive business outcomes (e.g., ranking, curation for featured posts, reputation).
- Public surface: Browsing profiles by `username`, fetching public posts, comments, and reactions are permitted without auth.
- Validation: Request DTOs use Jakarta validation; errors surfaced via `GlobalExceptionHandler`.

### Policy Examples (Admins – illustrative)

- Moderation workflow: Admins review content that violates community guidelines or is flagged by users (reports not implemented yet). Example policy: more than N unique reports within 24 hours triggers review; clear violations can be removed immediately per policy.
- Featured post curation: Use reaction signals to inform curation (e.g., weighted scores across reaction types) and recent engagement; selections can be refreshed daily/weekly. Implementation details are TBD and currently not present in code.
- Draft handling: `PRIVATE` posts are drafts—no public visibility, comments, or reactions. Publishing is achieved by switching visibility to `PUBLIC`.

References:

- Ownership checks: [dev_connect_backend/src/main/java/com/example/demo/security/AuthUtil.java](dev_connect_backend/src/main/java/com/example/demo/security/AuthUtil.java)
- Reaction logic: [dev_connect_backend/src/main/java/com/example/demo/reaction/ReactionService.java](dev_connect_backend/src/main/java/com/example/demo/reaction/ReactionService.java)
- Exceptions: [dev_connect_backend/src/main/java/com/example/demo/exception](dev_connect_backend/src/main/java/com/example/demo/exception)

## API Overview

Authentication & Profile

- POST `/auth/login` → JWT token. [AuthController](dev_connect_backend/src/main/java/com/example/demo/controller/AuthController.java)
- POST `/api/users/register` → Create user. [UserController](dev_connect_backend/src/main/java/com/example/demo/controller/UserController.java)
- GET `/api/users/my-profile` → Current user profile (auth required).
- GET `/api/users?username=` → Public profile lookup.
- PUT `/api/users/my-profile` → Update current user profile.

Posts

- POST `/api/posts` → Create post (auth required).
- GET `/api/posts/my-post` → Current user’s posts (auth required).
- GET `/api/posts?username=` → Posts by user.
- GET `/api/posts/public?page=&size=` → Paginated public posts.
- PUT `/api/posts/{postId}` → Update own post.
- DELETE `/api/posts/{postId}` → Delete own post.

Comments

- POST `/api/posts/{postId}/comments` → Add comment to public post.
- GET `/api/posts/{id}/comments` → List comments on a post.
- DELETE `/api/comments/{id}` → Delete own comment.

Reactions

- POST `/api/posts/{postId}/reactions` → React to a post.
- POST `/api/comments/{commentId}/reactions` → React to a comment.
- GET `/api/posts/{postId}/reactions` → Summaries for a post.
- GET `/api/comments/{commentId}/reactions` → Summaries for a comment.

Search

- GET `/api/search?keyword=&page=&size=` → Public posts by keyword (title/tags). [SearchController](dev_connect_backend/src/main/java/com/example/demo/controller/SearchController.java)
- Profiles are not searchable by skills/bio at present (intentional business choice).

OpenAPI UI

- `springdoc-openapi` is present; Swagger UI exposed under `/swagger-ui/**`, `/v3/api-docs/**`. See `SecurityConfig`.
  - PRD references `/swagger-ui.html`; this typically redirects to the UI and is compatible with the above paths.

## Auth & Security

- Stateless JWT auth with `JwtFilter`; login issues tokens via `JwtUtil`. Defaults: 1-hour expiration.
- Public endpoints include `register`, `login`, `search`, public posts/comments/reactions, and Swagger.
- Protected endpoints rely on `SecurityContext` principal; ownership enforced by repository queries and `AuthUtil`.

References:

- Config: [dev_connect_backend/src/main/java/com/example/demo/config/SecurityConfig.java](dev_connect_backend/src/main/java/com/example/demo/config/SecurityConfig.java)
- JWT: [dev_connect_backend/src/main/java/com/example/demo/security/JwtUtil.java](dev_connect_backend/src/main/java/com/example/demo/security/JwtUtil.java)
- Filter: [dev_connect_backend/src/main/java/com/example/demo/security/JwtFilter.java](dev_connect_backend/src/main/java/com/example/demo/security/JwtFilter.java)

## Data Storage & Infrastructure

- Database: PostgreSQL (Dockerized via `docker-compose.yml`); JPA/Hibernate with `ddl-auto=update`.
- No message brokers or external storage observed.
- Application properties support env overrides for DB URL/credentials and JWT secret/expiration.

References:

- Compose: [dev_connect_backend/docker-compose.yml](dev_connect_backend/docker-compose.yml)
- Build: [dev_connect_backend/build.gradle](dev_connect_backend/build.gradle)
- Config: [dev_connect_backend/src/main/resources/application.properties](dev_connect_backend/src/main/resources/application.properties)

## Integrations (Upstream & Downstream)

- Upstream clients: Angular SPA in `dev_connect_frontend` and any external consumers calling the REST API.
- Downstream systems: PostgreSQL database.
- Cross-microservice calls: None observed; this service does not call other services. All interactions are inbound HTTP calls to this service.
- Documentation tooling: Swagger UI via `springdoc-openapi`.

Confirmed integrations:

- No additional microservices or external providers integrate with this service at present.

## PRD Alignment Notes

- Profile endpoint name: PRD uses `GET /api/users/me`; implementation uses `GET /api/users/my-profile` for the authenticated user.
- Login endpoint path: PRD examples show `/api/auth/login`; implementation uses `/auth/login`.
- Search endpoints: PRD shows `/api/posts/search?...`; implementation provides `GET /api/search?keyword=...` over public posts (title and tags). Tags map to the PRD’s "tech stack" concept.
- Build tool: PRD allows Maven or Gradle; this repo uses Gradle.
- Frontend: PRD allows React or Angular; this repo includes an Angular app under `dev_connect_frontend`.

## Success Metrics (Business-Oriented)

- Engagement signals: Reaction rates and comment volume on public posts.
- Creation activity: Number of posts created (and drafts saved).
- Curation impact: Featured post selections and their subsequent engagement.
- Safety/quality: Admin moderation actions (once roles are enabled).

## Planned Extensions / Roadmap

- Admin role: Policy enforcement, moderation queues, and featured-post curation.
- Featured posts: Business curation surfaced to users; likely backed by reaction/engagement signals.
- Search expansion: Optional future inclusion of profile discovery (skills/bio) if business requires it.

## Non-Goals (Current)

- Monetization: No billing or paid tiers in scope currently; system is community/free-oriented.
- External integrations: No email, storage, or analytics integrations are implemented.

## Frontend Touchpoints

- The Angular app contains registration views and common UI components; it is expected to call the above endpoints for registration, login, and browsing public content.
- Routes: [dev_connect_frontend/src/app/app.routes.ts](dev_connect_frontend/src/app/app.routes.ts)
- Register component: [dev_connect_frontend/src/app/register/register.ts](dev_connect_frontend/src/app/register/register.ts)

## Error Handling

- Consistent JSON error responses via `GlobalExceptionHandler` (validation, auth failures, not found, access denied, DB downtime).
- Validation errors include field messages; enum deserialization errors list allowed values.

## Non-Functional Notes

- Stateless security; horizontal scaling is straightforward.
- Public endpoints may be cached at the edge (if added later), as they do not require auth.
- `ddl-auto=update` is convenient for development but consider migrations for production.

## Onboarding Quickstart

1. Start DB + app via Docker Compose or run locally against Postgres.
2. Register a user → Login → Use JWT in `Authorization: Bearer <token>` header.
3. Create posts, react, and comment; verify visibility and ownership rules.
4. Browse Swagger UI for endpoint contracts.

## Architecture Overview

Simple view of components and data flow:

```
+------------------+         HTTP (JSON)          +-------------------+
|  Angular Frontend|  <-------------------------> |  DevConnect API   |
|  (dev_connect_   |                               |  (Spring Boot)    |
|   frontend)      |                               |                   |
+------------------+                               |  Controllers      |
  |                                          |  Services         |
  |                                          |  Security (JWT)   |
  |                                          |  Repositories     |
  v                                          +---------+---------+
                JDBC / JPA     |
                   v
                 +---------------+
                 |  PostgreSQL   |
                 |  (Docker)     |
                 +---------------+
```

Key notes:

- Stateless JWT authentication between frontend and API.
- All persistent state lives in PostgreSQL via JPA/Hibernate.
- Public endpoints are accessible without auth; mutations and private data require JWT.

## Sequence: Create Post (Typical Flow)

```
User          Frontend (Angular)         DevConnect API                 PostgreSQL
 |                    |                         |                           |
 | 1. Auth token      |                         |                           |
 |    present         |                         |                           |
 |------------------->|                         |                           |
 |                    | 2. POST /api/posts      |                           |
 |                    |    {title,content,...}  |                           |
 |                    |------------------------>| 3. Verify JWT             |
 |                    |                         |    (JwtFilter/JwtUtil)    |
 |                    |                         | 4. Create Post entity     |
 |                    |                         |    (PostService)          |
 |                    |                         |-------------------------->|
 |                    |                         | 5. INSERT post row        |
 |                    |                         |<--------------------------|
 |                    |<------------------------| 6. 201 + PostResponse     |
 | 7. UI updates      |                         |                           |
```

Variations:

- Update/Delete follow the same JWT verification and ownership rules.
- Comments/Reactions allowed only on PUBLIC posts; PRIVATE is draft-only.

## Sequence: Login + JWT Validation

```
User          Frontend (Angular)          DevConnect API                Security Components            PostgreSQL
 |                    |                          |                               |                         |
 | 1. Enter creds     |                          |                               |                         |
 |    (username/pwd)  |                          |                               |                         |
 |------------------->|                          |                               |                         |
 |                    | 2. POST /auth/login      |                               |                         |
 |                    |    {username,password}   |------------------------------>| 3. Authenticate         |
 |                    |                          |   (AuthenticationManager      |   against UserDetails   |
 |                    |                          |    + DaoAuthenticationProvider)                           |
 |                    |                          |                               |------------------------>|
 |                    |                          |                               | 4. SELECT user (BCrypt) |
 |                    |                          |                               |<------------------------|
 |                    |                          | 5. Generate JWT (JwtUtil)     |                         |
 |                    |<-------------------------|    with expiration             |                         |
 | 6. Store token     |                          |                               |                         |
 |    (e.g., memory)  |                          |                               |                         |
 |                    |                          |                               |                         |
 |                    | 7. Protected request      |                               |                         |
 |                    |    with Authorization     |                               |                         |
 |                    |    header: Bearer <jwt>   |------------------------------>| 8. JwtFilter intercepts |
 |                    |                          |                               |    (skip public paths)  |
 |                    |                          |                               | 9. Validate token       |
 |                    |                          |                               |    (JwtUtil: subject,   |
 |                    |                          |                               |     expiry)             |
 |                    |                          |                               | 10. Load principal      |
 |                    |                          |                               |     (CustomUserService) |
 |                    |                          |                               |------------------------>|
 |                    |                          |                               | 11. SELECT user         |
 |                    |                          |                               |<------------------------|
 |                    |                          | 12. Set SecurityContext       |                         |
 |                    |                          |     and continue to controller|                         |
 |                    |<-------------------------| 13. Response                   |                         |
```

Notes:

- Invalid or expired token returns 401 with a JSON error (see JwtAuthenticationEntryPoint).
- Public endpoints (register, login, search, public posts/comments/reactions, Swagger) bypass JWT in `JwtFilter`.
