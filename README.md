# FinTrack

FinTrack is a Spring Boot REST API for tracking personal finances. It lets users organize accounts, record income and expenses, create monthly budgets, and view financial reports. Transactions automatically update account balances, including when a transaction is edited, deleted, or moved between accounts.

This project was built to learn backend application development with Java and Spring Boot while creating a practical application that could be used in everyday life.

## Features

- Register users with validated credentials and BCrypt password hashing
- View the authenticated user's profile
- Log in with email and password to receive a signed JWT
- Require JWT authentication for all non-authentication endpoints
- Create, read, update, and delete financial accounts
- Create, read, update, and delete income and expense categories
- Create, read, update, and delete transactions
- Automatically update account balances when transactions change
- Create and manage monthly category budgets
- Retrieve accounts, transactions, and budgets for the authenticated user
- Generate user-scoped monthly summaries, budget statuses, and account balance reports
- Validate incoming request data and return centralized error responses
- Test transaction balance rules and authentication boundaries with JUnit, Mockito, MockMvc, and H2

## Tech Stack

| Technology | Purpose |
| --- | --- |
| Java 25 | Application language |
| Spring Boot 4.1 | Application framework |
| Spring Web MVC | REST controllers and HTTP request handling |
| Spring Data JPA | Repository and database access layer |
| Hibernate | Object-relational mapping |
| Jakarta Validation | Request validation |
| Spring Security | Password hashing and endpoint protection |
| OAuth2 Resource Server | JWT creation and validation |
| PostgreSQL | Relational database |
| Flyway | Versioned database schema migrations |
| Docker Compose | Local PostgreSQL environment |
| Maven Wrapper | Build and dependency management |
| JUnit 5 and Mockito | Unit testing |

## Architecture

The application follows a layered structure:

```text
HTTP request
    -> Controller
    -> Service
    -> Repository
    -> PostgreSQL
```

Code is grouped by feature under `src/main/java/com/ajthapa`, including `auth`, `user`, `account`, `category`, `transaction`, `budget`, and `report` packages.

## API Overview

The application runs at `http://localhost:8080` by default.

| Method | Endpoint | Description |
| --- | --- | --- |
| `POST` | `/api/auth/register` | Register a user |
| `POST` | `/api/auth/login` | Log in and receive a JWT |
| `GET` | `/api/users/me` | Get the authenticated user |
| `GET`, `POST` | `/api/accounts` | List or create the authenticated user's accounts |
| `GET`, `PUT`, `DELETE` | `/api/accounts/{id}` | Read, update, or delete an account |
| `GET`, `POST` | `/api/categories` | List or create categories |
| `GET`, `PUT`, `DELETE` | `/api/categories/{id}` | Read, update, or delete a category |
| `GET`, `POST` | `/api/transactions` | List or create the authenticated user's transactions |
| `GET`, `PUT`, `DELETE` | `/api/transactions/{id}` | Read, update, or delete a transaction |
| `GET`, `POST` | `/api/budgets` | List or create the authenticated user's budgets |
| `GET`, `PUT`, `DELETE` | `/api/budgets/{id}` | Read, update, or delete a budget |
| `GET` | `/api/reports/monthly-summary` | Get a user's monthly totals and category spending |
| `GET` | `/api/reports/budget-status` | Compare a user's monthly spending with their budgets |
| `GET` | `/api/reports/account-balances` | List a user's current account balances |

Supported account types are `CHECKING`, `SAVINGS`, `CREDIT_CARD`, `CASH`, and `INVESTMENT_ACCOUNT`. Transaction and category types are `INCOME` and `EXPENSE`.

The registration and login endpoints are public. Every other endpoint requires an `Authorization: Bearer <token>` header. Account, transaction, budget, and report endpoints derive the current user from the signed token rather than accepting a user ID from the client. Category ownership is still a planned decision.

## API Examples

These examples use the HTTP request format supported by IntelliJ IDEA, VS Code REST Client, and similar API tools. Replace example IDs with the IDs returned by your database.

### 1. Register a user

```http
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "name": "Anuj Thapa",
  "email": "anuj@example.com",
  "password": "StrongPass1!"
}
```

### 2. Log in

```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "anuj@example.com",
  "password": "StrongPass1!"
}
```

The response contains a token with a one-hour lifetime. Store its `token` value as an HTTP-client variable for the remaining examples:

```http
@token = paste_token_here
```

### 3. View the current user

```http
GET http://localhost:8080/api/users/me
Authorization: Bearer {{token}}
```

### 4. Create an expense category

```http
POST http://localhost:8080/api/categories
Content-Type: application/json
Authorization: Bearer {{token}}

{
  "name": "Groceries",
  "type": "EXPENSE"
}
```

### 5. Create an account

```http
POST http://localhost:8080/api/accounts
Content-Type: application/json
Authorization: Bearer {{token}}

{
  "name": "Main Checking",
  "type": "CHECKING",
  "balance": 1000.00
}
```

### 6. Create a transaction

```http
POST http://localhost:8080/api/transactions
Content-Type: application/json
Authorization: Bearer {{token}}

{
  "categoryId": 1,
  "accountId": 1,
  "description": "Weekly groceries",
  "amount": 75.50,
  "type": "EXPENSE"
}
```

This expense decreases the selected account's balance by `75.50`. An `INCOME` transaction increases it.

### 7. Create a monthly budget

```http
POST http://localhost:8080/api/budgets
Content-Type: application/json
Authorization: Bearer {{token}}

{
  "categoryId": 1,
  "month": "2026-09",
  "limitAmount": 400.00
}
```

### 8. View user-scoped reports

```http
GET http://localhost:8080/api/reports/monthly-summary?year=2026&month=9
Authorization: Bearer {{token}}

###

GET http://localhost:8080/api/reports/budget-status?year=2026&month=9
Authorization: Bearer {{token}}

###

GET http://localhost:8080/api/reports/account-balances
Authorization: Bearer {{token}}
```

## Running Locally

### Prerequisites

- Java 25
- Docker Desktop with Docker Compose
- Git

### 1. Clone the repository

```bash
git clone https://github.com/AJT14-cmd/FinTrack.git
cd FinTrack
```

### 2. Create your local environment file

On Windows PowerShell:

```powershell
Copy-Item .env.example .env
```

On macOS or Linux:

```bash
cp .env.example .env
```

Open `.env`, replace both database password placeholders with the same local password, and replace the JWT placeholder with a Base64-encoded 256-bit secret. The file is ignored by Git and should never be committed.

```dotenv
POSTGRES_USER=fintrack_user
POSTGRES_PASSWORD=your_local_password
POSTGRES_DB=fintrack

SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5332/fintrack
SPRING_DATASOURCE_USERNAME=fintrack_user
SPRING_DATASOURCE_PASSWORD=your_local_password

JWT_SECRET=your_base64_encoded_256_bit_secret
```

Generate a suitable JWT secret with PowerShell:

```powershell
$bytes = New-Object byte[] 32
[Security.Cryptography.RandomNumberGenerator]::Fill($bytes)
[Convert]::ToBase64String($bytes)
```

On macOS or Linux, run `openssl rand -base64 32`.

### 3. Start PostgreSQL

```bash
docker compose up -d
```

PostgreSQL will be available on `localhost:5332`. Confirm that the container is running with:

```bash
docker compose ps
```

### 4. Start FinTrack

On Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

On macOS or Linux:

```bash
./mvnw spring-boot:run
```

The API is ready when the application reports that it started on port `8080`.

### 5. Stop the database

```bash
docker compose down
```

Flyway applies versioned migrations from `src/main/resources/db/migration` when the application starts. Hibernate uses `validate`, so it verifies that the migrated schema matches the JPA entities without recreating tables or deleting data.

> [!WARNING]
> A PostgreSQL database previously created by Hibernate may contain tables but no Flyway history. Back up any data you need before adopting the first migration. For disposable local data, run `docker compose down -v` once and then `docker compose up -d` to create a fresh database. The `-v` command permanently deletes the local database volume.

## Testing

Run the complete test suite:

On Windows PowerShell:

```powershell
.\mvnw.cmd test
```

On macOS or Linux:

```bash
./mvnw test
```

Tests use the `test` Spring profile and an in-memory H2 database. Flyway builds the H2 schema from the same migrations and Hibernate validates it before tests run. The suite does not connect to or modify the development PostgreSQL database.

## What I Learned

Building FinTrack gave me practical experience with:

- Structuring a Spring Boot application with controller, service, repository, entity, and DTO layers
- Designing REST endpoints and selecting appropriate HTTP methods and status codes
- Mapping Java entities and relationships to PostgreSQL with JPA and Hibernate
- Validating JSON request bodies with Jakarta Validation
- Handling application errors consistently with `@RestControllerAdvice`
- Keeping multi-step balance updates atomic with `@Transactional`
- Writing user-scoped Spring Data repository queries
- Testing business logic in isolation with JUnit and Mockito
- Managing local infrastructure and environment variables with Docker Compose
- Protecting database credentials from source control
- Hashing passwords with BCrypt and authenticating stateless requests with JWTs
- Managing schema changes with versioned Flyway migrations

## Planned Features

- Add PostgreSQL Testcontainers coverage for database migrations
- Expand unit, repository, and controller test coverage
- Complete ownership tests for authenticated account, transaction, budget, and report operations
- Make categories user-owned and remove unrestricted collection endpoints
- Add transaction filtering, sorting, pagination, and custom date ranges
- Publish interactive API documentation with OpenAPI and Swagger UI
- Add recurring transactions, savings goals, and spending trend reports
- Build a frontend dashboard for accounts, budgets, transactions, and reports
- Add CI checks and deploy the application
- Explore read-only bank account synchronization through a financial data provider
