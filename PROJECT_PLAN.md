# FinTrack Project Plan

Updated: September 15, 2026

## Vision

Build a personal finance application for everyday use and as a resume project.
Users can register, manage accounts, record income and expenses, set monthly
budgets, and understand spending through reports.

The first complete release uses manually entered financial data. Bank
synchronization is a later exploration, not a requirement for finishing v1.
Complete one milestone at a time and learn the concepts as you implement them.

## Current Position

The core backend is implemented. Authentication, migrations, and API
documentation are already present.

Completed foundations:

- [x] Spring Boot REST API with feature packages, controllers, services, repositories, and DTOs.
- [x] PostgreSQL persistence through Spring Data JPA and Hibernate.
- [x] Account, category, transaction, and budget CRUD operations.
- [x] Automatic balance updates when transactions are created, edited, deleted, or moved.
- [x] Monthly summaries, category spending, budget status, and account balance reports.
- [x] Request validation and centralized error responses.
- [x] BCrypt registration, JWT login, and the current-user endpoint.
- [x] Authenticated-user scoping for accounts, transactions, budgets, and reports.
- [x] Tests covering financial calculations and authentication/authorization behavior.
- [x] Isolated H2 test configuration.
- [x] Flyway migrations and Hibernate schema validation.
- [x] OpenAPI documentation and Swagger UI.
- [x] README and Docker Compose setup for PostgreSQL.

The backend provides substantial resume material. Category ownership is the
next gap to close before preparing a usable frontend.

## Milestone 1: User-Owned Categories

Status: Next task.

Categories currently have no owner and use unrestricted repository queries.
Authenticated users can access and modify shared categories.

Goal: Each user manages only their own categories. Their transactions and
budgets can reference only categories they own.

### Learn

- Many-to-one relationships: many categories belong to one user.
- Authentication identifies the caller; authorization limits access.
- Ownership must be checked for related resources as well as direct endpoints.
- Schema migrations must account for existing data.

### Build

1. Add an AppUser relationship to Category, following the account ownership pattern.
2. Add a new migration such as V2__add_category_ownership.sql with an owner column,
   foreign key, and suitable lookup index. Do not edit already-applied V1.
3. Plan how existing categories acquire owners before enforcing NOT NULL.
4. Add repository methods findAllByAppUserId and findByIdAndAppUserId.
5. Pass the authenticated user's identity through the controller/service flow,
   following the existing account implementation. Do not trust an owner ID from the request body.
6. Scope category list, read, create, update, and delete operations to that user.
7. Resolve categories by both ID and owner when creating or updating transactions and budgets.
8. Return 404 for missing or other-user categories, consistent with existing ownership behavior.
9. Update affected test fixtures, Swagger descriptions, and README examples.

### Existing Data

Do not assign all categories to an arbitrary user. A shared category may be
referenced by multiple users' transactions or budgets. To preserve data, plan
how to copy categories per owner and reconnect references before making
ownership mandatory.

A fresh development database is an option only if the existing data is
explicitly disposable. Resetting a Docker volume deletes that data and is not
required by this plan. Testing an empty database alone does not verify a
migration against populated data.

### Completion Checks

- [ ] New categories belong to the authenticated user.
- [ ] Lists contain only the user's categories.
- [ ] User A cannot read, edit, or delete User B's category.
- [ ] Transactions and budgets reject another user's category on create and update.
- [ ] Rejected requests leave balances and stored data unchanged.
- [ ] Missing and other-user categories produce the expected 404 responses.
- [ ] Migrations work with the chosen existing-data strategy.
- [ ] The complete test suite passes.

## Milestone 2: Transaction Filtering, Sorting, And Pagination

Status: Planned after category ownership.

Goal: Find transactions without downloading the entire history.

Learn query parameters, Spring Data pagination, and combining query conditions.

1. Add pagination with a default and maximum page size.
2. Add predictable sorting with an ID tie-breaker when dates match.
3. Add account, category, type, and date-range filters.
4. Validate dates, pagination values, and supported sorting fields.
5. Preserve user ownership restrictions in every query.
6. Document parameters and the paginated response in OpenAPI.

Example planned request:

```http
GET /api/transactions?page=0&size=20&type=EXPENSE&sort=transactionDateTime,desc
```

Completion: Filters and pagination work together, invalid inputs produce clear
errors, and no filter combination exposes another user's transactions.

## Milestone 3: Continuous Integration

Status: Planned.

Goal: GitHub runs the build and tests automatically on pushes and pull requests.

- [ ] Add GitHub Actions using the project's Java version and Maven Wrapper.
- [ ] Run the full test suite with the isolated test profile.
- [ ] Require no development database credentials or personal JWT secret.
- [ ] Preserve test reports when a run fails.
- [ ] Verify the workflow succeeds from a clean checkout.

Completion: A failing test fails the workflow and a clean checkout builds successfully.

## Milestone 4: PostgreSQL Integration Tests

Status: Planned.

Testcontainers starts temporary PostgreSQL containers for tests. Keep fast
unit/H2 tests and add focused coverage against the actual database engine.

- [ ] Configure an isolated PostgreSQL Testcontainer.
- [ ] Verify Flyway builds a fresh database and Hibernate validates it.
- [ ] Test populated-data upgrades supported by the migration strategy.
- [ ] Test ownership queries and important database constraints.
- [ ] Include representative transaction/balance persistence checks.
- [ ] Run these tests in CI with Docker available.

Completion: PostgreSQL behavior is verified without accessing the development database.

## Milestone 5: First Usable Frontend

Status: Planned after the preceding backend milestones.

Goal: Complete this workflow in a browser:

```text
Register -> Log in -> Create account -> Create category
-> Add expense -> See updated balance -> Review monthly spending
```

Learn basic HTML, CSS, JavaScript, forms, and HTTP requests if those are new.
Choose a framework after understanding these fundamentals.

Build screens in this order:

1. Registration and login.
2. Current-user loading and authenticated navigation.
3. Account listing and creation.
4. Category management.
5. Transaction list with filters, pagination, and create/edit/delete forms.
6. Monthly budgets and spending status.
7. Monthly summary and account balance dashboard.

Plan token handling and expiration behavior before connecting login. Configure
CORS for the actual frontend origin if it runs separately from the backend.

Handle loading, empty results, validation errors, server errors, and expired
authentication. Confirm destructive deletions.

Completion: The workflow works on desktop and mobile without Swagger or an HTTP client.

## Milestone 6: Deploy And Polish v1

Status: Planned.

- [ ] Deploy backend, frontend, and PostgreSQL.
- [ ] Configure production secrets outside source control.
- [ ] Use HTTPS and the correct frontend/API origins.
- [ ] Establish database backups and verify a restore.
- [ ] Verify migrations and the main workflow in the deployed environment.
- [ ] Update setup instructions, screenshots, API links, and architecture notes.
- [ ] Provide a demo with fictional financial data.
- [ ] Document limitations and tag a v1 release.

## Definition Of Finished

FinTrack v1 is finished when users can register, manage their own accounts,
categories, transactions, and budgets, and view accurate reports through a
usable frontend. Other users' data must remain inaccessible.

A reviewer should be able to follow the README, run the project from a clean
checkout, explore the API documentation, and see passing automated checks.
Deployment and a documented backup/restore process complete the personal-use release.

The backend can be presented on a resume now with an accurate description of
implemented features. Finishing v1 does not require every future idea below.

## Later Enhancements

- Recurring transactions.
- Savings goals and spending trends.
- CSV import/export.
- Read-only bank synchronization, subject to provider access and data freshness.
- Additional account recovery and session-management features as usage grows.

## Working Routine

1. Define expected behavior with concrete examples.
2. Implement one small workflow through controller, service, and repository.
3. Test business rules, ownership boundaries, and failure cases.
4. Run relevant checks and the full suite before completing the milestone.
5. Update README and OpenAPI documentation when behavior changes.
6. Review the diff for mistakes and secrets, then commit and push when ready.
7. Mark a milestone complete only when its completion checks pass.
