# Commerce Order Service

RESTful API for an e-commerce order management system built with Java 21 and Spring Boot 3.

## Tech Stack

- Java 21
- Spring Boot 3
  - Spring Web
  - Spring Security
  - Spring Data JPA
  - Validation
  - Actuator
- PostgreSQL
- Hibernate (JPA)
- Flyway
- Lombok
- JWT (access + refresh token flow)
- OpenAPI / Swagger UI

## Architecture

The project follows a strict layered structure:

- `controller`
- `service`
- `repository`

Supporting packages:

- `entity`
- `dto`
- `mapper`
- `security`
- `config`
- `exception`

## Implemented Features

- Authentication and authorization
  - Register, login, refresh, logout endpoints
  - JWT stateless auth
  - Role-based product write access (admin only)
- Product APIs
  - List products (authenticated users)
  - Create/update products (admin only)
- Order APIs
  - Transactional order placement
  - Stock deduction with rollback on insufficient stock
  - Users can only view their own orders
- Global error handling with structured JSON responses
- Request correlation (`X-Request-Id`)
- OpenAPI docs and Actuator endpoints

## Core Entities

- `User`
- `Product`
- `Order`
- `OrderItem`
- `RefreshToken`

## Local Setup

### 1) Start PostgreSQL

Example with Docker:

```bash
docker run --name order-db \
  -e POSTGRES_DB=commerce_order_db \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 -d postgres:16
```

### 2) Configure Environment Variables (optional)

Defaults are already defined in `application.yml`, but you can override:

```bash
export DB_URL=jdbc:postgresql://localhost:5432/commerce_order_db
export DB_USERNAME=postgres
export DB_PASSWORD=postgres
export JWT_SECRET='replace-with-strong-secret-at-least-32-bytes'
export JWT_ACCESS_TOKEN_EXPIRATION_MS=3600000
export JWT_REFRESH_TOKEN_EXPIRATION_MS=604800000
```

### 3) Run the application

```bash
mvn spring-boot:run
```

## API Documentation

After starting the app:

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Actuator Endpoints

- `GET /actuator/health`
- `GET /actuator/info`
- `GET /actuator/metrics`
- `GET /actuator/prometheus`

## Authentication Quickstart

### Register

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"user1","password":"Password123","email":"user1@test.com"}'
```

### Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user1","password":"Password123"}'
```

### Refresh token

```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"<refresh-token>"}'
```

### Logout

```bash
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"<refresh-token>"}'
```

## Database Migrations

Flyway migrations are in:

- `src/main/resources/db/migration/V1__init_schema.sql`
- `src/main/resources/db/migration/V2__add_refresh_tokens.sql`

## Testing

Run test suite:

```bash
mvn test
```

CI workflow runs:

```bash
mvn -B clean verify
```
