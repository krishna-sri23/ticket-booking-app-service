# Movie Ticket Booking Platform — Service

Backend service for the online movie ticket booking platform (XYZ case study).

## Tech Stack
- Java 17, Spring Boot 3.2
- MySQL 8 (via Docker)
- Flyway for DB migrations
- Maven
- Lombok, Jakarta Validation
- Springdoc OpenAPI (Swagger UI)

## Architecture

```
Controller -> Service -> Repository -> MySQL
                  |
                  +-> DiscountEngine -> RuleEvaluator (Strategy)
```

- **Concurrency:** Pessimistic locking on `show_seat` during booking (plus optimistic `@Version` as a safety net).
- **Discounts:** Configurable via DB (`offer`, `offer_rule`, `offer_city`, `offer_theatre` tables) + Strategy pattern for rule evaluators.
- **Audit:** `created_at` / `updated_at` via JPA auditing.

## Running the app

### Option 1 — Full Docker Compose (app + MySQL)
```bash
docker-compose up --build
```
App available at: `http://localhost:8080`
Swagger UI: `http://localhost:8080/swagger-ui.html`

### Option 2 — MySQL in Docker, app locally
```bash
docker-compose up -d mysql
./mvnw spring-boot:run
```

### Option 3 — Local everything
Install MySQL 8 locally, create DB `booking_platform`, update `application.yml`, then:
```bash
./mvnw spring-boot:run
```

## Key Endpoints

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/api/v1/movies/{movieId}/shows?cityId=&date=` | **READ scenario**: browse theatres & timings for a movie |
| GET | `/api/v1/shows/{showId}/seats` | Get seat availability for a show |
| POST | `/api/v1/bookings` | **WRITE scenario**: book tickets with auto discount |
| GET | `/api/v1/bookings/{bookingId}` | Booking details |
| DELETE | `/api/v1/bookings/{bookingId}` | Cancel booking and release seats |
| POST | `/api/v1/admin/shows` | Admin: create show (auto-provisions seat inventory) |
| PUT | `/api/v1/admin/shows/{showId}` | Admin: update show |
| DELETE | `/api/v1/admin/shows/{showId}` | Admin: delete show (no active bookings) |
| GET | `/api/v1/offers` | List active offers |
| GET | `/health` | Health check |

### Sample: Browse shows
```bash
curl "http://localhost:8080/api/v1/movies/1/shows?cityId=1&date=2026-04-15"
```

### Sample: Book tickets
```bash
curl -X POST http://localhost:8080/api/v1/bookings \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "showId": 2,
    "showSeatIds": [21, 22, 23]
  }'
```
(Show 2 is an afternoon show → 20% off offer applies. With 3 seats, "50% off 3rd ticket" offer also applies.)

## Offers (Seeded)

| Code | Description |
|---|---|
| THIRD_TICKET_50 | 50% off on the 3rd ticket (most expensive of 3+) |
| AFTERNOON_20 | 20% off on afternoon shows (12:00 - 17:00) |
| MUMBAI_PVR_100 | Flat ₹100 off at PVR Mumbai (non-stackable) |

## Project Structure
```
src/main/java/com/bookingplatform/
├── BookingPlatformApplication.java
├── config/            OpenAPI, etc.
├── controller/        REST controllers
├── dto/               Request / Response DTOs
├── entity/            JPA entities (+ BaseEntity for audit)
├── enums/             Shared enums
├── exception/         Custom exceptions + global handler
├── repository/        Spring Data JPA repos
└── service/
    ├── ShowBrowseService     (READ)
    ├── BookingService        (WRITE — with tx + locking)
    ├── OfferService
    └── discount/
        ├── DiscountEngine
        ├── DiscountContext
        ├── AppliedOffer
        └── rule/             (Strategy implementations)
            ├── RuleEvaluator              (interface)
            ├── TimeRangeRuleEvaluator
            ├── DayOfWeekRuleEvaluator
            ├── MinTicketsRuleEvaluator
            ├── MovieRuleEvaluator
            └── LanguageRuleEvaluator

src/main/resources/
├── application.yml
└── db/migration/
    ├── V1__initial_schema.sql
    └── V2__seed_data.sql
```

## Design Patterns Used
- **Strategy** — `RuleEvaluator` implementations (one per rule type)
- **Builder** — Lombok `@Builder` on entities and DTOs
- **Repository** — Spring Data JPA
- **DTO** — Separate API contract from entities

## Non-Functional Concerns Addressed
- **Transactional integrity** — `@Transactional` + pessimistic row locking
- **Concurrency** — `@Version` optimistic locking as secondary defence
- **Validation** — Jakarta Bean Validation on request DTOs
- **Observability** — SLF4J logging, health endpoint
- **Documentation** — OpenAPI / Swagger UI
- **Data integrity** — Foreign keys, unique constraints, indexes
- **Extensibility** — New rule types need only a new `RuleEvaluator` class

## Not Implemented (Intentional)
- Payment gateway integration (design-only)
- Seat hold with TTL (design-only)
- Auth/JWT (assumes external SSO)
- Caching layer (Redis)
- Bulk booking / theatre admin CRUD (optional write scenarios)

See `TODO.md` and `SCHEMA.md` for full design context.
