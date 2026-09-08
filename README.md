# FinTrack

FinTrack is a Spring Boot REST API for tracking personal finances. It lets users organize accounts, record income and expenses, create monthly budgets, and view financial reports. Transactions automatically update account balances, including when a transaction is edited, deleted, or moved between accounts.

This project was built to learn backend application development with Java and Spring Boot while creating a practical application that could be used in everyday life.

## Features

- Create and view users
- Create, read, update, and delete financial accounts
- Create, read, update, and delete income and expense categories
- Create, read, update, and delete transactions
- Automatically update account balances when transactions change
- Create and manage monthly category budgets
- Retrieve accounts, transactions, and budgets for a specific user
- Generate user-scoped monthly summaries, budget statuses, and account balance reports
- Validate incoming request data and return centralized error responses
- Test transaction balance rules with JUnit and Mockito

## Tech Stack

| Technology | Purpose |
| --- | --- |
| Java 25 | Application language |
| Spring Boot 4.1 | Application framework |
| Spring Web MVC | REST controllers and HTTP request handling |
| Spring Data JPA | Repository and database access layer |
| Hibernate | Object-relational mapping |
| Jakarta Validation | Request validation |
| PostgreSQL | Relational database |
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

Code is grouped by feature under `src/main/java/com/ajthapa`, including `user`, `account`, `category`, `transaction`, `budget`, and `report` packages.

## API Overview

The application runs at `http://localhost:8080` by default.

| Method | Endpoint | Description |
| --- | --- | --- |
| `GET` | `/api/users` | List users |
| `GET` | `/api/users/{id}` | Get one user |
| `POST` | `/api/users` | Create a user |
| `GET` | `/api/users/{userId}/accounts` | List a user's accounts |
| `GET` | `/api/users/{userId}/transactions` | List a user's transactions |
| `GET` | `/api/users/{userId}/budgets` | List a user's budgets |
| `GET`, `POST` | `/api/accounts` | List or create accounts |
| `GET`, `PUT`, `DELETE` | `/api/accounts/{id}` | Read, update, or delete an account |
| `GET`, `POST` | `/api/categories` | List or create categories |
| `GET`, `PUT`, `DELETE` | `/api/categories/{id}` | Read, update, or delete a category |
| `GET`, `POST` | `/api/transactions` | List or create transactions |
| `GET`, `PUT`, `DELETE` | `/api/transactions/{id}` | Read, update, or delete a transaction |
| `GET`, `POST` | `/api/budgets` | List or create budgets |
| `GET`, `PUT`, `DELETE` | `/api/budgets/{id}` | Read, update, or delete a budget |
| `GET` | `/api/reports/monthly-summary` | Get a user's monthly totals and category spending |
| `GET` | `/api/reports/budget-status` | Compare a user's monthly spending with their budgets |
| `GET` | `/api/reports/account-balances` | List a user's current account balances |

Supported account types are `CHECKING`, `SAVINGS`, `CREDIT_CARD`, `CASH`, and `INVESTMENT_ACCOUNT`. Transaction and category types are `INCOME` and `EXPENSE`.

## API Examples

These examples use the HTTP request format supported by IntelliJ IDEA, VS Code REST Client, and similar API tools. Replace example IDs with the IDs returned by your database.

### 1. Create a user

```http
POST http://localhost:8080/api/users
Content-Type: application/json

{
  "name": "Anuj Thapa",
  "email": "anuj@example.com"
}
```

### 2. Create an expense category

```http
POST http://localhost:8080/api/categories
Content-Type: application/json

{
  "name": "Groceries",
  "type": "EXPENSE"
}
```

### 3. Create an account

```http
POST http://localhost:8080/api/accounts
Content-Type: application/json

{
  "name": "Main Checking",
  "type": "CHECKING",
  "balance": 1000.00,
  "appUserId": 1
}
```

### 4. Create a transaction

```http
POST http://localhost:8080/api/transactions
Content-Type: application/json

{
  "categoryId": 1,
  "accountId": 1,
  "description": "Weekly groceries",
  "amount": 75.50,
  "type": "EXPENSE"
}
```

This expense decreases the selected account's balance by `75.50`. An `INCOME` transaction increases it.

### 5. Create a monthly budget

```http
POST http://localhost:8080/api/budgets
Content-Type: application/json

{
  "categoryId": 1,
  "appUserId": 1,
  "month": "2026-09",
  "limitAmount": 400.00
}
```

### 6. View user-scoped reports

```http
GET http://localhost:8080/api/reports/monthly-summary?year=2026&month=9&userId=1

###

GET http://localhost:8080/api/reports/budget-status?year=2026&month=9&userId=1

###

GET http://localhost:8080/api/reports/account-balances?userId=1
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

Open `.env` and replace both password placeholders with the same local password. The file is ignored by Git and should never be committed.

```dotenv
POSTGRES_USER=fintrack_user
POSTGRES_PASSWORD=your_local_password
POSTGRES_DB=fintrack

SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5332/fintrack
SPRING_DATASOURCE_USERNAME=fintrack_user
SPRING_DATASOURCE_PASSWORD=your_local_password
```

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

> [!WARNING]
> The current development configuration uses `spring.jpa.hibernate.ddl-auto=create-drop`. Hibernate recreates the database schema when the application starts and removes it when the application stops. Do not use this setting for production or for data you need to preserve.

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

Tests use the `test` Spring profile and an in-memory H2 database. Running the test suite does not connect to or modify the development PostgreSQL database.

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

## Planned Features

- Add an isolated test profile with Testcontainers or a dedicated test database
- Expand unit, repository, and controller test coverage
- Replace automatic schema recreation with Flyway database migrations
- Add user registration and login with Spring Security and JWT authentication
- Make categories user-owned and remove unrestricted collection endpoints
- Add transaction filtering, sorting, pagination, and custom date ranges
- Publish interactive API documentation with OpenAPI and Swagger UI
- Add recurring transactions, savings goals, and spending trend reports
- Build a frontend dashboard for accounts, budgets, transactions, and reports
- Add CI checks and deploy the application
- Explore read-only bank account synchronization through a financial data provider
