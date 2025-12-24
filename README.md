# Mini Bank (Full Stack)

## Overview
Mini Bank is a lightweight full‑stack banking demo with JWT authentication, account
management, transfers, and transaction history. The repository contains:
- `mini-bank` (Spring Boot API)
- `mini-bank-ui` (React + Vite client)

## Prerequisites
- Java 21
- Node.js 18+ (or 20+)
- Docker + Docker Compose

## Quick Start (Fresh Clone)
```bash
git clone <your-repo-url>
cd <repo-root>
```

### 1) Start PostgreSQL
```bash
docker compose -f mini-bank/docker-compose.yml up -d
```

### 2) Run the Backend
```bash
cd mini-bank
./mvnw spring-boot:run
```
API will be available at `http://localhost:8080`.

Swagger UI:
- `http://localhost:8080/swagger-ui.html`

### 3) Run the Frontend
```bash
cd ../mini-bank-ui
npm install
npm run dev
```
UI will be available at `http://localhost:5173`.

## Environment Configuration
Backend (`mini-bank/src/main/resources/application.yaml` defaults):
- `SPRING_DATASOURCE_URL` (default: `jdbc:postgresql://localhost:5432/mini_bank`)
- `SPRING_DATASOURCE_USERNAME` (default: `mini_bank`)
- `SPRING_DATASOURCE_PASSWORD` (default: `mini_bank`)
- `JWT_SECRET` (32+ chars for local dev)
- `JWT_ACCESS_TOKEN_MINUTES` (default: `60`)

Frontend (`mini-bank-ui/.env`):
```bash
VITE_API_URL=http://localhost:8080
```

## Verifying the Stack
- Register a user via UI (`/register`), login (`/login`).
- Create accounts, transfer funds, and view history.
- Check API docs in Swagger UI.

## Useful Commands
Backend tests:
```bash
cd mini-bank
./mvnw test
```

Frontend build:
```bash
cd mini-bank-ui
npm run build
```

## Notes
- Account deletion is blocked when balance is not zero.
- Transfers are concurrency‑safe.
- Search/sort/page state is reflected in the UI URL query params.
