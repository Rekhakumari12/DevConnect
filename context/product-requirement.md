# 🧠 Project Overview: DevConnect – Developer Collaboration Platform

## Goal
Build a mini collaboration platform for developers to share project ideas, comment, and react to others’ posts — similar to a lightweight internal version of Dev.to or GitHub Discussions.

This project introduces participants to the Spring Boot ecosystem, REST APIs, persistence, authentication, and optional frontend integration using React or Angular.

---

## 🏗️ Key Objectives

Learn and apply Java + Spring Boot fundamentals:
- Dependency Injection
- REST API design
- Spring Data JPA & Hibernate
- Security with Spring Security + JWT
- Validation & exception handling

Learn application architecture concepts:
- Entity–Repository–Service–Controller layering
- DTO mapping
- Testing using JUnit & MockMVC

Integrate backend with a frontend client (React or Angular):
- Consume REST APIs using Axios or Fetch
- Manage authentication tokens in the frontend

Use local developer-friendly tools:
- Database: PostgreSQL (via Docker or local install)
- API Testing: Postman or cURL
- IDE: IntelliJ or VSCode (Java extensions)
- Version control: GitHub

---

## ⚙️ Technical Stack

| Layer | Technology |
|-----|-----------|
| Language | Java 17+ |
| Framework | Spring Boot 3.x |
| Build Tool | Maven or Gradle |
| Database | PostgreSQL |
| ORM | Spring Data JPA + Hibernate |
| Security | Spring Security + JWT |
| Validation | Hibernate Validator |
| API Docs | SpringDoc OpenAPI (Swagger UI) |
| Testing | JUnit 5, Mockito, MockMVC |
| Dev Environment | Docker Compose (optional) |
| Frontend | React or Angular |
| Version Control | Git + GitHub |
| Deployment (optional) | Render / Railway |

---

## 🧩 Functional Modules & Requirements

### 1. User Management

Users can register with `username`, `email`, `password`, and `skills` (comma-separated). Passwords must be hashed using BCrypt. Users can log in and receive a JWT token. Authenticated users can view their profile at `GET /api/users/me` and update basic profile information such as skills and bio. JWT tokens must be validated on all protected endpoints.

Technologies used include Spring Security, JWT, BCryptPasswordEncoder, and validation annotations.

---

### 2. Project Ideas (Posts)

Authenticated users can create project ideas with a title, description, tech stack, and visibility setting (PUBLIC or PRIVATE). Users can view all public posts and their own posts (public and private). Only the owner can edit or delete their posts. Each post includes creation and update timestamps.

This module uses Spring Data JPA, entities, repositories, and relationships.

---

### 3. Comments

Users can comment on public posts. Each comment is linked to both the post and the user. Users are allowed to delete only their own comments.

---

### 4. Reactions

Users can react to posts using one of the following reactions: Like, Funny, Celebrate, or Support. Each user can react once per post, and toggling a reaction updates the existing record. API responses include total reaction counts and whether the current user has reacted.

---

### 5. Search & Filter

Users can search public posts by tech stack keywords or title substrings. Results are paginated with a default size of 10 posts per page. Custom JPA queries and Pageable are used.

---

### 6. API Documentation

Swagger UI is available at `/swagger-ui.html`. Each endpoint includes example request and response payloads using SpringDoc OpenAPI.

---

### 7. Error Handling & Validation

Invalid input returns structured JSON error responses in the following format:

```json
{
  "timestamp": "2025-11-07T10:00:00Z",
  "status": 400,
  "error": "Validation Error",
  "message": "Email must be valid"
}
