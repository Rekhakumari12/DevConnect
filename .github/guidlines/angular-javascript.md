# Angular & TypeScript Coding Standards

## Purpose

This document defines Angular and TypeScript coding standards for frontend development. It mirrors the structure, depth, and intent of the React & JavaScript Coding Standards, but is adapted to Angular’s framework patterns, TypeScript-first approach, and RxJS-based data flow.

---

## Table of Contents

1. TypeScript Language Standards
2. Angular Core Principles
3. Project Structure
4. Modules and Standalone Components
5. Component Design
6. Templates and Data Binding
7. Services and Dependency Injection
8. State Management
9. HTTP and API Integration
10. Routing
11. Forms and Validation
12. Error Handling
13. Styling
14. Testing
15. Performance
16. Accessibility
17. Security

---

## TypeScript Language Standards

Always write Angular code in TypeScript with strict type checking enabled. This prevents runtime errors and improves readability.

Use `const` by default, `let` only when reassignment is required, and never use `var`.

Example:  
const API_URL = '/api';  
let page = 0;

Define clear interfaces or types for all data models instead of using loosely typed objects.

Example:  
interface Group {  
 id: string;  
 name: string;  
 members: Member[];  
}

Avoid `any`. If the type is unknown, use `unknown` and narrow it explicitly.

Use optional chaining and nullish coalescing to handle nullable values safely.

Example:  
const memberName = group?.members?.[0]?.name ?? 'Unknown';

---

## Angular Core Principles

Use Angular CLI for project setup and code generation to ensure consistency.

Prefer standalone components for new development unless there is a strong reason to group functionality in modules.

Follow unidirectional data flow: data comes in via inputs, actions go out via events.

Keep business logic out of templates. Templates should describe structure, not behavior.

---

## Project Structure

Organize the project by features, not by file type.

Recommended structure:

src/  
app/  
core/ global singleton services, guards, interceptors  
shared/ reusable components, pipes, directives  
features/  
groups/  
expenses/  
app.component.ts  
app.routes.ts  
main.ts

File naming rules:

- Components: kebab-case.component.ts
- Services: kebab-case.service.ts
- Templates: kebab-case.component.html
- Styles: kebab-case.component.css

---

## Modules and Standalone Components

Prefer standalone components for simplicity and better tree-shaking.

A standalone component should declare its own dependencies using `imports`.

Use NgModules only when:

- Maintaining legacy code
- Building shared libraries
- Strong feature isolation is required

---

## Component Design

Each component must have a single responsibility and remain easy to understand.

Components should:

- Be under ~200 lines of TypeScript
- Receive data via `@Input`
- Emit actions via `@Output`

Example pattern:

- Parent fetches data
- Child displays data
- Child emits user actions
- Parent reacts to actions

Avoid direct DOM manipulation. Use bindings instead.

---

## Templates and Data Binding

Use interpolation for text and property binding for attributes.

Prefer simple template expressions. Do not perform filtering, sorting, or complex logic directly in HTML.

Good:
{{ title }}

Bad:
{{ users.filter(u => u.active).length }}

Use structural directives clearly:

- `*ngIf` for conditional rendering
- `*ngFor` for lists with `trackBy` to improve performance

---

## Services and Dependency Injection

All API calls and shared business logic must live in services.

Services should:

- Be injectable
- Be stateless where possible
- Return Observables, not subscriptions

Components decide when and how to subscribe.

Never call HTTP APIs directly inside components.

---

## State Management

Use local component state for UI-only concerns like loading flags and form values.

For shared or cross-component state:

- Start with services and RxJS subjects
- Escalate to NgRx only when state becomes complex or difficult to reason about

Avoid global mutable state.

---

## HTTP and API Integration

Use Angular HttpClient via services only.

Handle loading, success, and error states explicitly in components.

Use HTTP interceptors for:

- Authentication headers
- Global error handling
- Logging

Do not duplicate error-handling logic across components.

---

## Routing

Define routes per feature whenever possible.

Use lazy loading for feature routes to reduce initial bundle size.

Always handle invalid routes with a fallback (NotFound component).

Use ActivatedRoute to safely read route parameters.

---

## Forms and Validation

Prefer Reactive Forms for all but the simplest forms.

Reactive Forms advantages:

- Explicit state
- Better validation
- Easier testing

Define validators in the component, not in the template.

Display validation errors only after the field is touched or the form is submitted.

---

## Error Handling

Always handle errors from Observables.

Never assume API calls will succeed.

Use a global ErrorHandler for uncaught exceptions and logging.

Show user-friendly error messages, not raw stack traces.

---

## Styling

Prefer component-scoped styles.

Use global styles only for:

- CSS reset
- Typography
- Theme variables

Avoid inline styles except for truly dynamic values.

Keep CSS simple and readable.

---

## Testing

Write unit tests for:

- Components with logic
- Services with API calls
- Pipes and directives with behavior

Follow the Arrange–Act–Assert pattern in tests.

Mock services instead of calling real APIs.

---

## Performance

Use `trackBy` in `*ngFor` loops.

Avoid unnecessary subscriptions. Unsubscribe when required or use async pipe.

Lazy load features and heavy components.

Avoid expensive computations in templates.

---

## Accessibility

Use semantic HTML elements like `button`, `nav`, `article`, and `header`.

Always provide labels for inputs.

Ensure keyboard navigation works for all interactive elements.

Use ARIA attributes only when native semantics are insufficient.

---

## Security

Never trust user input.

Avoid bypassing Angular’s built-in sanitization.

Do not store sensitive data like passwords or tokens in localStorage unless absolutely required.

Prefer HttpOnly cookies for authentication when possible.

---

## Common Pitfalls

- Putting logic in templates
- Using `any` everywhere
- Subscribing inside services
- Forgetting to unsubscribe
- Overusing global state
- Skipping error handling
- Not lazy loading features
- Mixing presentation and business logic

---

## References

Angular Documentation: https://angular.dev/overview  
RxJS Documentation: https://rxjs.dev/guide/overview  
TypeScript Handbook: https://www.typescriptlang.org/docs/handbook/intro.html  
Angular Style Guide: https://angular.dev/style-guide
