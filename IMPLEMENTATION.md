# Movie Ticket Booking Platform — Implementation Document

**Candidate:** Krishana Mohan Srivastava
**Repository:** https://github.com/krishna-sri23/ticket-booking-app-service
**Submission Date:** 2026-04-15

---

## 1. Overview

This document accompanies the code submission for the XYZ Movie Ticket Booking Platform case study. It summarizes scope, design decisions, assumptions, and trade-offs. Two companion documents ship with the repo:

- `ARCHITECTURE.md` — HLD, sequence flows, NFR design (scaling, availability, security, payment)
- `SCHEMA.md` / `schema.mmd` — ER diagram and DDL notes
- `README.md` — how to run, endpoint map, project structure

---

## 2. Scope Covered

| Category | Status | Notes |
|---|---|---|
| **READ scenario** (mandatory) | ✅ Implemented | Browse theatres/timings for a movie in a city by date |
| **WRITE scenario** (good-to-have) | ✅ Implemented | Book tickets with seat locking + auto discount |
| Theatre admin CRUD for shows | ✅ Implemented | Create / update / delete with safety checks |
| Cancel booking | ✅ Implemented | Releases seats, sets status CANCELLED |
| Offer/discount engine (configurable) | ✅ Implemented | DB-driven offers + rules, Strategy-based evaluators |
| Unit tests | ✅ Partial | DiscountEngine + rule evaluators (core logic) |
| OpenAPI / Swagger UI | ✅ Implemented | `/swagger-ui.html` |
| HLD + sequence diagrams | ✅ Documented | `ARCHITECTURE.md` |
| Scalability / availability / security / payment | ✅ Design-only | `ARCHITECTURE.md` §5–8 |
| **Skipped / design-only:** payment gateway, Redis cache, seat-hold TTL, JWT auth, Kafka fan-out | ⏩ Intentional | Adapter interfaces sketched; focus kept on core booking correctness |

---

## 3. Tech Stack

| Layer | Choice | Why |
|---|---|---|
| Language | Java 17 | Modern LTS, records + sealed types available |
| Framework | Spring Boot 3.2 | Industry default; mature JPA/validation support |
| DB | MySQL 8 | Widely deployed, ACID, row-level pessimistic locking |
| Migrations | Flyway | Versioned, additive, safe for prod |
| ORM | Spring Data JPA / Hibernate | Repository pattern, declarative transactions |
| API docs | Springdoc OpenAPI | Auto-generated from annotations |
| Build | Maven (wrapper) | Reproducible with `./mvnw` |
| Container | Docker + docker-compose | One-command local setup |
| Tests | JUnit 5 + Mockito | Standard Spring Boot test stack |

---

## 4. High-Level Architecture

```
Clients → API Gateway → Load Balancer → booking-svc (N pods, stateless)
                                              │
                     ┌────────────────────────┼──────────────────────┐
                     │                        │                      │
                 MySQL (primary +      Redis (cache + seat-hold)  Kafka (async
                 read replicas)                                   fan-out — design)
```

- **Stateless service** — horizontal scale via Kubernetes HPA.
- **Pessimistic row locking** on `show_seat` prevents double booking; `@Version` optimistic lock is a safety net.
- **DB-driven discount engine** — offers configurable without redeploy; rule evaluators plug in via Strategy.
- **Read/write split** possible at service level (`readOnly=true` on browse queries).

Full diagram and justification: `ARCHITECTURE.md` §1–2.

---

## 5. Data Model (Key Entities)

```
city ── theatre ── screen ── seat
                         └── show ── show_seat
                                         ▲
                                         │
user ── booking ── booking_seat ─────────┘
            └── booking_offer ── offer ── offer_rule
                                     └─── offer_city / offer_theatre
```

Highlights:
- `show_seat` denormalizes per-show seat state (status, price, @Version) so booking is a fast indexed lookup.
- `offer`/`offer_rule`/`offer_city`/`offer_theatre` let ops define offers in DB without code changes.
- Every table has `created_at` / `updated_at` via `BaseEntity` + JPA auditing.
- Full ERD: `SCHEMA.md`, `schema.mmd`.

---

## 6. API Contracts

Swagger UI: `http://localhost:8080/swagger-ui.html`

### 6.1 Browse shows (READ scenario)
```
GET /api/v1/movies/{movieId}/shows?cityId={cityId}&date={yyyy-MM-dd}
→ 200 [
    {
      "theatreId": 1, "theatreName": "PVR Phoenix", "address": "...",
      "screens": [
        { "screenId": 1, "screenName": "Screen 1",
          "timings": [
            { "showId": 10, "startTime": "14:30", "endTime": "17:00",
              "basePrice": 250.00, "availableSeats": 18 }
          ] }
      ]
    }
  ]
```

### 6.2 Get seat map
```
GET /api/v1/shows/{showId}/seats
→ 200 [ { "showSeatId": 101, "seatNumber": "A1", "rowLabel": "A",
          "seatType": "REGULAR", "status": "AVAILABLE", "price": 250.00 }, ... ]
```

### 6.3 Book tickets (WRITE scenario)
```
POST /api/v1/bookings
{ "userId": 1, "showId": 2, "showSeatIds": [21, 22, 23] }
→ 201 {
    "bookingId": 55, "status": "CONFIRMED",
    "bookedSeats": ["A1","A2","A3"],
    "totalAmount": 750.00, "discount": 175.00, "finalAmount": 575.00,
    "appliedOffers": [
      { "code": "AFTERNOON_20", "discountApplied": 150.00 },
      { "code": "THIRD_TICKET_50", "discountApplied": 125.00 }
    ]
  }
```
Errors: 404 (user/show/seat missing), 409 (seat unavailable / optimistic lock), 400 (validation).

### 6.4 Booking details / cancel
```
GET    /api/v1/bookings/{id}
DELETE /api/v1/bookings/{id}      → cancels, releases seats
```

### 6.5 Admin: show CRUD
```
POST   /api/v1/admin/shows            (auto-provisions show_seat inventory)
PUT    /api/v1/admin/shows/{id}       (refuses price/screen change with active bookings)
DELETE /api/v1/admin/shows/{id}       (refuses if active bookings exist)
```

### 6.6 Offers
```
GET /api/v1/offers?cityId=&theatreId=
```

---

## 7. Design Patterns Used

| Pattern | Where | Why |
|---|---|---|
| **Strategy** | `RuleEvaluator` + 5 implementations (TimeRange, DayOfWeek, MinTickets, Movie, Language) | New rule types = new class, zero changes to engine |
| **Builder** | Lombok `@Builder` on entities + DTOs | Readable construction, immutable-ish DTOs |
| **Repository** | Spring Data JPA | Declarative data access, abstracts persistence |
| **DTO** | `dto/request`, `dto/response` | Decouples API contract from entities |
| **Adapter** (design-only) | `PaymentGateway` interface + Razorpay/Stripe adapters | Swap payment provider without touching booking logic |
| **Template** (implicit) | `@Transactional` | Standard tx boundary around business ops |

---

## 8. Concurrency & Correctness

The hardest problem in this domain is **preventing double booking** under load. Two defenses:

1. **Pessimistic row lock** — `SELECT ... FOR UPDATE` on the specific `show_seat` rows a booking wants. Concurrent bookings for the same seats block until the first commits. Chosen over app-level distributed locks because it's DB-native, atomic with the transaction, and free from lock-server availability risk.
2. **Optimistic `@Version`** — safety net for edge cases (e.g., if locking is ever downgraded, or two requests squeeze past in a race window). Triggers `OptimisticLockingFailureException` → mapped to HTTP 409.

Everything (booking row, booking_seats, booking_offers, show_seat status update) commits in **one** `@Transactional` boundary — isolation `READ_COMMITTED`.

Trade-off: pessimistic locking hurts throughput on very hot shows. For a production blockbuster release, the design proposes seat-hold TTL in Redis + gateway queuing (see ARCHITECTURE.md §5).

---

## 9. Discount Engine

DB-driven so ops can add offers without code changes:

- **Scope**: `offer_city` / `offer_theatre` (empty list = global).
- **Rules** (ALL must pass): stored as JSON in `offer_rule.rule_value`, interpreted by a matching `RuleEvaluator`.
- **Application**: `PER_BOOKING`, `PER_TICKET`, or `NTH_TICKET` (Nth most expensive seat, customer-friendly).
- **Stackability**: `stackable` flag; non-stackable offers short-circuit the chain.
- **Priority**: lower number evaluated first.

Seeded examples:
- `THIRD_TICKET_50` — 50 % off the 3rd (most expensive) ticket when ≥ 3 seats
- `AFTERNOON_20` — 20 % off shows between 12:00 and 17:00
- `MUMBAI_PVR_100` — flat ₹100 off at PVR Mumbai (non-stackable)

Adding a new rule type = one new class implementing `RuleEvaluator` — engine picks it up automatically via Spring DI.

---

## 10. Non-Functional Design (summary — full detail in ARCHITECTURE.md)

| Concern | Approach |
|---|---|
| **Scalability** | Stateless pods + HPA, MySQL read replicas for browse, Redis for hot show caching, shard by `city_id` when needed |
| **Availability (99.99%)** | Multi-AZ app + DB, circuit breakers on external calls (Resilience4j), strict timeouts, blue/green deploys, additive-only Flyway migrations |
| **Security** | TLS everywhere, SSO/JWT at gateway, `@PreAuthorize` on admin endpoints, parameterized JPA queries, Jakarta Validation, OWASP Top 10 mitigations (table in ARCHITECTURE.md §8), secrets in KMS/Vault |
| **Payment integration** | `PaymentGateway` interface with Razorpay/Stripe adapters. Flow: create booking in `PENDING` → return `PaymentIntent` → webhook confirms → sweeper releases stale `PENDING` after 10 min. Idempotency keys on bookings |
| **Observability** | Micrometer → Prometheus/Grafana, structured JSON logs → ELK, OpenTelemetry traces → Jaeger, correlation IDs end-to-end |
| **Monetization** | Convenience fee per ticket, platform fee from theatre, sponsored placements, subscription, ads |

---

## 11. Assumptions

1. **Authentication is external** — SSO/JWT terminates at the API gateway; service trusts the `userId` in the request. Real deploy would use `@PreAuthorize("#userId == authentication.principal.userId")`.
2. **Payment is out of scope** for implementation — booking commits directly as `CONFIRMED`. Production would go `PENDING` → webhook-confirm → `CONFIRMED`.
3. **Seat pricing formula** — `base_price × {REGULAR:1.0, PREMIUM:1.5, VIP:2.0}`. Simple and deterministic; real world would use per-seat or per-category price tables.
4. **One currency** (INR). Multi-currency would need a currency field + FX at booking time.
5. **Discount offers evaluate at booking time only**, not pre-quoted to the user (UI would call a separate quote endpoint — not implemented).
6. **Cancellation is free and instant** — real product would have a refund policy + payment-gateway refund call.
7. **No timezone handling** — times stored as local DB time. Real deploy would use UTC + theatre tz metadata.

---

## 12. Trade-offs Considered

| Decision | Alternatives weighed | Why chosen |
|---|---|---|
| Pessimistic lock on `show_seat` | Optimistic-only / distributed lock / Redis | DB-native, atomic with tx, no extra infra dependency |
| `show_seat` denormalized per show | Compute availability from `seat` + `booking_seat` join | Faster reads, simpler locking semantics, trivially extensible (price/status per show) |
| DB-driven offers with JSON rule values | Hard-coded if/else / rules engine (Drools) | Ops-friendly, type-safe enough for the 5 rule types we need, no extra runtime |
| Monolith service | Microservices (booking / inventory / offers split) | Overkill for assignment; well-factored internal boundaries keep future split cheap |
| Flyway additive migrations only | Liquibase / schema tools | Works well with blue/green, no rollback scripts needed |
| Auto-provision `show_seat` on show create | Separate endpoint for inventory allocation | One call creates a fully bookable show; matches natural admin workflow |

---

## 13. What I Would Do Next (if extending)

1. **Integration tests** with Testcontainers (MySQL) covering the full booking + cancel path under concurrency.
2. **Seat hold with Redis TTL** so UI can reserve seats for 5 min while the user pays.
3. **Wire a real payment adapter** (Razorpay sandbox) with webhook handler.
4. **Auth** — JWT validation filter + method-level `@PreAuthorize`.
5. **Observability** — Micrometer + actuator endpoints enabled, Prometheus scrape config.
6. **Quote endpoint** — `POST /api/v1/bookings/quote` to show discounts before commit.
7. **Browse caching** — Redis with TTL ~30s on the browse endpoint.

---

## 14. Running the Project

```bash
# One-command local run (MySQL + app)
docker-compose up --build
# → http://localhost:8080 (redirects to Swagger)
# → http://localhost:8080/swagger-ui.html

# Run tests
./mvnw test
```

Sample requests in `README.md`.

---

## 15. Repository Layout

```
ticket-booking-app-service/
├── ARCHITECTURE.md           HLD, sequence flows, NFR design
├── IMPLEMENTATION.md         this document
├── README.md                 how to run, endpoints, structure
├── SCHEMA.md                 ER diagram + DDL notes
├── TODO.md                   original scope checklist
├── Dockerfile
├── docker-compose.yml
├── pom.xml  /  mvnw
└── src/
    ├── main/
    │   ├── java/com/bookingplatform/
    │   │   ├── config/       OpenAPI, etc.
    │   │   ├── controller/   REST edge
    │   │   ├── dto/          request / response
    │   │   ├── entity/       JPA entities + BaseEntity (audit)
    │   │   ├── enums/
    │   │   ├── exception/    GlobalExceptionHandler + custom exceptions
    │   │   ├── repository/   Spring Data JPA
    │   │   └── service/
    │   │       ├── ShowBrowseService       (READ)
    │   │       ├── BookingService          (WRITE — tx + locking)
    │   │       ├── AdminShowService        (admin CRUD)
    │   │       ├── OfferService
    │   │       └── discount/
    │   │           ├── DiscountEngine
    │   │           ├── DiscountContext / AppliedOffer
    │   │           └── rule/               (Strategy impls)
    │   └── resources/
    │       ├── application.yml
    │       └── db/migration/
    │           ├── V1__initial_schema.sql
    │           └── V2__seed_data.sql
    └── test/java/...         JUnit 5 + Mockito tests
```
