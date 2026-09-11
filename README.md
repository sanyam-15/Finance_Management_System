# Personal Finance Manager

Spring Boot 3 REST API for tracking income, expenses, savings goals, and financial reports. Users authenticate with session cookies; every resource is scoped to the authenticated account.

## Tech stack

| Component | Choice |
|-----------|--------|
| Language | Java 17 |
| Framework | Spring Boot 3.2.4 |
| Security | `spring-boot-starter-security` (session cookies) |
| Persistence | Spring Data JPA + PostgreSQL (H2 for tests) |
| Build | Maven |
| Tests | JUnit 5, Mockito, JaCoCo (80% line coverage) |

## Architecture

Layered design:

`Controller → Service → Repository`

- **DTOs** carry request/response payloads; JPA entities stay internal.
- **`@ControllerAdvice`** maps domain errors to 400/401/403/404/409.
- Configuration lives in `src/main/resources/application.properties` and environment variables.

### Design decisions

- **Session auth, not JWT** — assignment requires cookie sessions (`JSESSIONID`).
- **Hard delete for transactions** — deleted rows never appear in goals or reports.
- **Default categories are global** (`user_id` null) so they cannot be edited or deleted.
- **Goal progress** = `max(0, income − expenses)` from `startDate` through today.
- **Double JSON formatting** in `JacksonConfig` matches the official test script (raw `0` for zero `netSavings`/`currentProgress`).

## Local setup

### Prerequisites

- JDK 17+
- Maven 3.9+
- PostgreSQL 15+ (or Docker)

### Database

Create a database named `finance_manager`, or use Compose:

```bash
docker compose up db -d
```

Default connection (override with env vars):

```
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/finance_manager
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres
```

`docker-compose.yml` uses password `password` for the app container.

### Run the API

```bash
mvn spring-boot:run
```

Base URL: `http://localhost:8080/api`

### Tests and coverage

```bash
mvn test
```

JaCoCo writes `target/site/jacoco/index.html`. The build fails if line coverage is below 80%.

## API overview

All endpoints except register and login require a valid session cookie.

### Auth

| Method | Path | Status |
|--------|------|--------|
| POST | `/api/auth/register` | 201, 400, 409 |
| POST | `/api/auth/login` | 200, 401 — sets `JSESSIONID` |
| POST | `/api/auth/logout` | 200, 401 |

Register body:

```json
{
  "username": "user@example.com",
  "password": "password123",
  "fullName": "John Doe",
  "phoneNumber": "+1234567890"
}
```

### Transactions

| Method | Path | Notes |
|--------|------|--------|
| POST | `/api/transactions` | Amount > 0, date not in the future |
| GET | `/api/transactions` | Newest first. Filters: `startDate`, `endDate`, `category`, `categoryId`, `type` |
| PUT | `/api/transactions/{id}` | Date cannot be changed |
| DELETE | `/api/transactions/{id}` | Permanent delete |

### Categories

Default (immutable): Salary (INCOME); Food, Rent, Transportation, Entertainment, Healthcare, Utilities (EXPENSE).

| Method | Path |
|--------|------|
| GET | `/api/categories` |
| POST | `/api/categories` |
| DELETE | `/api/categories/{name}` |

Custom names are unique per user. Categories in use cannot be deleted. Default categories return 403.

### Goals

Progress: net savings since `startDate`. `startDate` defaults to today. Target date must be in the future and not before start date.

| Method | Path |
|--------|------|
| POST | `/api/goals` |
| GET | `/api/goals` |
| GET | `/api/goals/{id}` |
| PUT | `/api/goals/{id}` |
| DELETE | `/api/goals/{id}` |

### Reports

| Method | Path |
|--------|------|
| GET | `/api/reports/monthly/{year}/{month}` |
| GET | `/api/reports/yearly/{year}` |

Error status codes: **400** validation, **401** unauthenticated, **403** other user's data (goals), **404** missing resource, **409** duplicate username/category.

## Official e2e script

```bash
bash financial_manager_tests.sh http://localhost:8080/api
```

Against a deployed instance:

```bash
bash financial_manager_tests.sh https://YOUR-SERVICE.onrender.com/api
```

## Deploy on Render

1. Push this repo to GitHub.
2. Create a **PostgreSQL** database on Render.
3. Create a **Web Service** from the repo (or use `render.yaml`).
4. Set environment variables from the database:
   - `SPRING_DATASOURCE_URL` (jdbc URL; Render often gives `postgres://` — convert to `jdbc:postgresql://HOST:PORT/DB`)
   - `SPRING_DATASOURCE_USERNAME`
   - `SPRING_DATASOURCE_PASSWORD`
5. Build command: `./mvnw clean package -DskipTests` or `mvn clean package -DskipTests`
6. Start command: `java -jar target/finance-manager-0.0.1-SNAPSHOT.jar`

Docker:

```bash
docker compose up --build
```

The Docker image uses Java 21 JRE; the project compiles with Java 17.

## Project layout

```
src/main/java/com/syfe/financemanager/
  controller/   REST endpoints
  service/      business rules
  repository/   Spring Data JPA
  dto/          request/response objects
  model/        entities
  security/     session auth
  exception/    @ControllerAdvice
  config/       Jackson number formatting
```
