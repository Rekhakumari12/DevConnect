# Copilot Instructions - DevConnect

This repository contains the DevConnect collaboration platform. This file provides quick navigation to all AI agent guidance documents.

## Documentation Structure

### **Start Here: Project Context**

#### [**Technical Context**](../context/technical-context.md)

**Purpose**: Complete system architecture, data flows, and technical decisions  
**What it covers**:

- System overview and architecture (Java/Spring Boot backend + Angular frontend)
- PostgreSQL data model and JPA persistence strategy
- API endpoint documentation
- Docker Compose deployment
- Intentional legacy patterns (for learning)
- Development workflow and debugging

**When to use**:

- First time working in this codebase
- Understanding how backend and frontend interact
- Debugging issues
- Making architectural decisions
- Understanding the workshop learning objectives

#### [**Business Domain Context**](../context/business-domain-context.md)

**Purpose**: Domain concepts, business logic, and DevConnect collaboration workflows  
**What it covers**:

- Core domain concepts (Users, Profiles, Posts, Comments, Reactions)
- Visibility rules: PUBLIC vs PRIVATE (draft-only)
- Reaction semantics and outcomes (ranking, curation)
- Roles and governance (admins planned)
- Public vs protected endpoints and ownership enforcement

**When to use**:

- Understanding domain concepts
- Implementing new features
- Validating business logic
- Understanding settlement algorithm

---

### **Coding Standards & Guidelines**

#### [**Core Standards**](./guidlines/core-standards.md) ⭐ READ FIRST

**Purpose**: Universal coding principles applicable to ALL languages and projects  
**What it covers**:

- General principles (KISS, YAGNI, DRY, Boy Scout Rule, SOLID)
- Code smell detection and elimination
- Method/class size guidelines
- Naming conventions and readability requirements
- Cross-functional requirements (error handling, logging, security, performance, i18n)
- Testing best practices

**When to consider**:

- **Before writing ANY code** - these are mandatory principles
- When reviewing code quality
- Resolving design decisions
- Identifying code smells
- Refactoring legacy code

#### [**Java & Spring Boot Standards**](./guidlines/java-spring-boot.md)

**Purpose**: Java 17 and Spring Boot 3.2 best practices for the **backend service**  
**What it covers**:

- Java language standards (modern Java features, type system, null safety)
- Spring Boot patterns (dependency injection, component annotations)
- Layered architecture (Controller → Service → Repository)
- Spring Data JPA with PostgreSQL
- DTOs and validation (Bean Validation)
- Exception handling and global error handling
- Transaction management
- Testing (JUnit 5, Mockito, Testcontainers)
- Logging and configuration

**When to use**:

- Working on the `backend/` service
- Writing Spring Boot controllers, services, or repositories
- Working with MongoDB entities
- Implementing REST API endpoints
- Writing backend tests

#### [**Angular & JavaScript Standards**](./guidlines/angular-javascript.md)

**Purpose**: Angular and modern JavaScript best practices for the **frontend service**  
**What it covers**:

- Modern JavaScript (ES6+): const/let, arrow functions, destructuring, async/await
- Angular components, modules, and dependency injection
- State and data flow with RxJS Observables
- Component design patterns and smart/presentational separation
- API integration with Angular HttpClient
- Angular Router for navigation
- Forms and validation (Reactive Forms)
- Error handling strategies
- Testing (Angular Testing Library, Jasmine/Karma)
- Performance and accessibility

**When to use**:

- Working on the `frontend/` service
- Writing Angular components
- Managing state and side effects
- Integrating with backend APIs
- Writing frontend tests

---

## Repository Structure

```
DevConnect/
├── dev_connect_backend/        # Spring Boot backend (Java 17, Gradle, PostgreSQL)
│   ├── src/
│   │   ├── main/java/com/example/   # Controllers, services, repositories, entities, config
│   │   └── test/java/com/example/   # Backend tests
│   ├── build.gradle             # Gradle build
│   ├── docker-compose.yml       # Postgres + app (backend scope)
│   └── Dockerfile               # Backend container image
├── dev_connect_frontend/       # Angular frontend (TypeScript)
│   ├── src/                     # Angular app source
│   ├── angular.json
│   └── package.json
├── context/                    # Documentation
│   ├── technical-context.md
│   └── business-domain-context.md
└── github/
	└── guidlines/             # Coding standards (note: folder name is 'guidlines')
		├── core-standards.md
		├── java-spring-boot.md
		└── angular-javascript.md
```

---

## Recommended Workflow

### For Backend Development (Java/Spring Boot)

1. **First visit**: Read [technical-context.md](../context/technical-context.md)
2. **Before coding**: Review [core-standards.md](./guidlines/core-standards.md)
3. **While coding**: Follow [java-spring-boot.md](./guidlines/java-spring-boot.md)
4. **Always**: From `dev_connect_backend/`, run `./gradlew test` before committing
5. **Testing**: Write unit tests for services, integration tests for repositories

### For Frontend Development (Angular/JavaScript)

1. **First visit**: Read [technical-context.md](../context/technical-context.md)
2. **Before coding**: Review [core-standards.md](./guidlines/core-standards.md)
3. **While coding**: Follow [angular-javascript.md](./guidlines/angular-javascript.md)
4. **Always**: Run `npm test` before committing
5. **Testing**: Write component tests with Angular Testing Library (or Jasmine/Karma)

### Full Stack Development

1. **Understanding the system**: Read both technical and business context
2. **Backend changes**: Java/Spring Boot guidelines
3. **Frontend changes**: Angular/JavaScript guidelines
4. **Integration**: Test end-to-end flow using Docker Compose (backend directory)
5. **Running locally**:

   From the backend folder:

   ```bash
   cd dev_connect_backend
   docker compose up --build
   ```

---

## Quick Reference by Task

| Task                | Guidelines to Follow                                                                                               |
| ------------------- | ------------------------------------------------------------------------------------------------------------------ |
| REST API endpoint   | [Core](./guidlines/core-standards.md) → [Java/Spring Boot](./guidlines/java-spring-boot.md) § Controller Layer     |
| Service layer logic | [Core](./guidlines/core-standards.md) → [Java/Spring Boot](./guidlines/java-spring-boot.md) § Service Layer        |
| JPA repository      | [Java/Spring Boot](./guidlines/java-spring-boot.md) § Repository Layer                                             |
| Domain entity       | [Java/Spring Boot](./guidlines/java-spring-boot.md) § Domain Models                                                |
| Angular component   | [Core](./guidlines/core-standards.md) → [Angular/JavaScript](./guidlines/angular-javascript.md) § Component Design |
| API integration     | [Angular/JavaScript](./guidlines/angular-javascript.md) § API Integration                                          |
| Form handling       | [Angular/JavaScript](./guidlines/angular-javascript.md) § Forms and Validation                                     |
| State management    | [Angular/JavaScript](./guidlines/angular-javascript.md) § State Management                                         |
| Error handling      | [Core](./guidlines/core-standards.md) § Error Handling + service-specific guide                                    |
| Testing             | [Core](./guidlines/core-standards.md) § Testing + service-specific guide                                           |

---

## Code Review Checklist

Before submitting code for review:

- [ ] **Core Standards**: Applied SOLID principles, no code smells, small methods/classes
- [ ] **Types**: Backend uses proper Java types, Frontend uses strong TypeScript types
- [ ] **Error Handling**: Comprehensive error handling with proper logging
- [ ] **Security**: Input validation, no hardcoded secrets, sanitized data
- [ ] **Testing**: Unit tests for services/utilities, component tests for Angular
- [ ] **Logging**: Structured logging with appropriate levels
- [ ] **Documentation**: Complex logic has explanatory comments (WHY, not WHAT)
- [ ] **Performance**: No N+1 queries, proper indexing, resource cleanup
- [ ] **Backend**: DTOs for API, transaction management, Bean Validation
- [ ] **Frontend**: Angular components with RxJS, proper state management

---

## Additional Resources

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Data JPA](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [Angular Documentation](https://angular.dev/)
- [Angular Router](https://angular.dev/guide/routing)
- [springdoc-openapi](https://springdoc.org/)
