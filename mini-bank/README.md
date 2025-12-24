# Mini Bank Backend

## Overview
Mini Bank is a RESTful API for a lightweight banking app. It covers user registration,
JWT-based authentication, account management, and money transfers with transaction history.

## Highlights
- JWT-secured user registration and login
- Account CRUD with search by number/name
- Transfers by account number with concurrency-safe balance updates
- Transaction history per account
- Standardized error responses
- OpenAPI/Swagger documentation

## Tech Stack
- Java 21
- Spring Boot 3.5.8
- Spring Security + JWT
- Spring Data JPA + PostgreSQL
- springdoc OpenAPI

## Project Structure
- `src/main/java/com/main/mini_bank` - application source
- `src/main/resources/application.yaml` - default configuration
- `docker-compose.yml` - local PostgreSQL

## Getting Started (Fresh Clone)
1) Clone the repository and enter the backend directory:
```bash
git clone <your-repo-url>
cd <repo-root>/mini-bank
```

2) Start PostgreSQL with Docker:
```bash
docker compose up -d
```

3) Run the API:
```bash
./mvnw spring-boot:run
```

4) Verify the API and docs:
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI spec: `http://localhost:8080/v3/api-docs`

## Configuration
Environment variables (defaults shown):
- `SPRING_DATASOURCE_URL` = `jdbc:postgresql://localhost:5432/mini_bank`
- `SPRING_DATASOURCE_USERNAME` = `mini_bank`
- `SPRING_DATASOURCE_PASSWORD` = `mini_bank`
- `JWT_SECRET` = local default is provided for dev only; use 32+ chars in real usage
- `JWT_ACCESS_TOKEN_MINUTES` = `60`

## API Reference (Quick)
Authentication:
- `POST /api/users/register`
- `POST /api/users/login`

Accounts:
- `POST /api/accounts` (create)
- `GET /api/accounts` (search with `?number=&name=`)
- `GET /api/accounts/{id}`
- `PUT /api/accounts/{id}`
- `DELETE /api/accounts/{id}` (blocked if balance != 0)

Transactions:
- `POST /api/transactions/transfer`
- `GET /api/transactions/account/{accountId}`

## Error Response Shape
Errors are standardized as:
```json
{
  "timestamp": "2025-12-24T00:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/accounts",
  "fieldErrors": [
    { "field": "name", "message": "must not be blank" }
  ]
}
```

## Common Commands
Run tests:
```bash
./mvnw test
```

Build a jar:
```bash
./mvnw package
```

## Notes
- Transfers use row-level locking to prevent race conditions.
- Account deletion is blocked when balance is not zero.