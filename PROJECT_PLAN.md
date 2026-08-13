# PocketLedger Project Plan

This project is designed for someone who knows Java but is new to application development and Spring Boot.

The goal is to build a small but real personal finance tracking backend. You will learn how Java code becomes an application that accepts requests, stores data, follows a clean structure, and can eventually be polished for a resume.

## What You Are Building

You are building a backend application called **PocketLedger**.

A backend application handles:

- Data
- Business rules
- Users
- Security
- Databases
- API requests

For example, if someone uses a finance app and clicks "Add expense," the backend receives that request and saves the expense.

At first, this app will not have buttons or pages. You will interact with it using a browser, Postman, Insomnia, or another API testing tool. That is normal for backend development.

## The Simplest Version

The first version should do only this:

```text
Let someone save and view transactions.
```

A transaction is one money event:

```text
Coffee: $4.50 expense
Paycheck: $2000 income
Rent: $1200 expense
```

Start small. The first version does not need login, a database, Docker, deployment, or a frontend.

## Big Picture

A typical Spring Boot backend is organized like this:

```text
Controller -> Service -> Repository -> Database
```

For now, think of it like this:

```text
Controller = receives the request
Service = decides what should happen
Repository = saves or loads data
Database = stores data permanently
```

At the very beginning, you will skip the repository and database. Instead, you will store transactions temporarily in memory using Java collections.

## Stage 1: Run A Spring Boot App

### Learn

- What a Spring Boot project looks like
- How to start the app
- What a local server is
- What `localhost` means
- How to open an endpoint in the browser

### Build

```text
GET /api/health
```

Example response:

```text
PocketLedger is running
```

### Why This Matters

This proves your application is alive and able to respond to web requests.

## Stage 2: Create Your First API Endpoint

### Learn

- What HTTP is
- What `GET` means
- What a controller is
- How Java methods can respond to web requests

### Build

```text
GET /api/hello
```

Example response:

```json
{
  "message": "Hello from PocketLedger"
}
```

### Why This Matters

This is the first step from writing a Java program to building a web application.

## Stage 3: Create A Transaction Model

### Learn

- How to represent application data using Java classes
- Why applications have models
- How JSON maps to Java objects

### Build

A Java class representing a transaction:

```java
public class Transaction {
    private Long id;
    private String description;
    private BigDecimal amount;
    private String type;
}
```

Example JSON:

```json
{
  "id": 1,
  "description": "Coffee",
  "amount": 4.50,
  "type": "EXPENSE"
}
```

### Why This Matters

Real applications are built around data models.

## Stage 4: Save Transactions In Memory

### Learn

- How to store application data while the app is running
- Why in-memory storage is temporary
- How services contain business logic

### Build

```text
POST /api/transactions
GET /api/transactions
GET /api/transactions/{id}
DELETE /api/transactions/{id}
```

These endpoints allow you to:

- Create a transaction
- View all transactions
- View one transaction
- Delete a transaction

For now, the app can store transactions in a Java `Map`.

When the app stops, the data disappears. That is okay for this stage.

## Stage 5: Organize The App Properly

### Learn

Common application folders:

```text
controller
service
model
dto
exception
```

What they mean:

```text
controller = receives web requests
service = contains business rules
model = represents app data
dto = request and response shapes
exception = handles errors
```

### Why This Matters

This is how you keep the application from becoming messy as it grows.

## Stage 6: Add Validation

### Learn

- How to reject bad input
- Why real applications cannot trust user input
- How validation protects your app from invalid data

This request should fail:

```json
{
  "description": "",
  "amount": -10,
  "type": "EXPENSE"
}
```

Rules:

```text
Description cannot be blank
Amount must be greater than zero
Type must be INCOME or EXPENSE
```

## Stage 7: Add A Database

Only add PostgreSQL after the basic API makes sense.

### Learn

- What a database is
- What a table is
- What an entity is
- What Spring Data JPA does
- What a repository is

### Build

A transaction table that stores:

```text
id
description
amount
type
transaction_date
created_at
```

### Why This Matters

Now your data survives after the application stops.

## Stage 8: Add Categories

### Learn

- How different pieces of data relate to each other
- How one transaction can belong to one category
- How to model relationships in an application

Example categories:

```text
Food
Rent
Transportation
Salary
Entertainment
```

Example relationships:

```text
Coffee -> Food
Paycheck -> Salary
Rent -> Rent
```

### Why This Matters

Applications are usually relationships between objects, not just one object by itself.

## Stage 9: Add Reports

### Learn

- How to write business logic
- How to calculate useful values from stored data
- How reporting endpoints work

### Build

```text
GET /api/reports/monthly-summary
```

Example response:

```json
{
  "totalIncome": 3000,
  "totalExpenses": 1800,
  "netSavings": 1200
}
```

### Why This Matters

This makes the app more than basic create, read, update, and delete operations.

## Stage 10: Add Budgets

### Learn

- How to build a feature an actual user would care about
- How to compare budget limits against spending totals
- How to return calculated status values

### Build

Example budgets:

```text
Food budget: $400/month
Entertainment budget: $100/month
```

Example result:

```text
You spent $375 of your $400 Food budget.
```

## Stage 11: Add Login Later

Authentication is important, but it is not beginner-friendly as a starting point.

Add this only after the basic app works.

### Learn

- Registering users
- Logging in
- Password hashing
- JWT tokens
- User-specific transactions

### Build

```text
POST /api/auth/register
POST /api/auth/login
GET /api/users/me
```

## First Week Goal

Focus only on this:

```text
1. Create the Spring Boot project.
2. Run it locally.
3. Make /api/health return a message.
4. Make /api/transactions return an empty list.
5. Make POST /api/transactions save a transaction in memory.
```

Do not worry about:

- Databases
- Login
- Docker
- Deployment
- Fancy frontend
- Microservices
- Cloud hosting

Those come later.

## First Mental Model

Remember this:

```text
A Java class can become a web endpoint.
A web endpoint receives a request.
A request can contain JSON.
Spring Boot turns that JSON into Java objects.
Your Java code does something with those objects.
Spring Boot turns your Java response back into JSON.
```

That is the main idea. Once you understand that, Spring Boot becomes much less intimidating.

## Recommended First Build

Build this exact version first:

```text
PocketLedger v0.1

Features:
- App starts
- GET /api/health returns "OK"
- GET /api/transactions returns all saved transactions
- POST /api/transactions creates a transaction
- Data is stored in memory
```

When this works, you will have crossed an important line: you will no longer just know Java syntax. You will have built the beginning of a real application.

## Resume-Ready Finish Line

The project is polished enough for a resume when someone can:

```text
1. Clone the repository.
2. Run the app locally.
3. Open the API documentation.
4. Add transactions and categories.
5. View a monthly financial summary.
6. Understand the project from the README.
```

Later polish features:

- PostgreSQL database
- Spring Data JPA
- Validation
- Error handling
- Swagger/OpenAPI documentation
- Tests
- Docker
- Deployment
- Authentication
