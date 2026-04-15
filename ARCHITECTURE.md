# Architecture — Movie Ticket Booking Platform

## 1. High-Level Architecture (HLD)

```
                ┌──────────────────────────┐
                │   Clients (Web / Mobile) │
                └──────────────┬───────────┘
                               │ HTTPS
                      ┌────────▼────────┐
                      │   API Gateway   │  (TLS, rate limit, authN via SSO/JWT)
                      └────────┬────────┘
                               │
                 ┌─────────────▼──────────────┐
                 │       Load Balancer         │
                 └─────────────┬──────────────┘
                               │
           ┌───────────────────┼───────────────────┐
           │                   │                   │
   ┌───────▼──────┐    ┌───────▼──────┐    ┌───────▼──────┐
   │ booking-svc  │    │ booking-svc  │    │ booking-svc  │  (this service — stateless)
   │  (pod 1..N)  │    │  (pod 1..N)  │    │  (pod 1..N)  │
   └───┬──────┬───┘    └───┬──────┬───┘    └───┬──────┬───┘
       │      │            │      │            │      │
       │      │  ┌─────────▼──────▼────────────▼──────▼────┐
       │      └─►│  Redis (seat-hold TTL cache, offer cache)│
       │         └───────────────────┬─────────────────────┘
       │                             │
  ┌────▼─────────────────────────────▼─────┐
  │  MySQL 8 — Primary (writes)            │
  │  └── Read replicas (browse/read APIs)  │
  └────────────────────┬───────────────────┘
                       │ CDC / async events
             ┌─────────▼──────────┐
             │  Kafka (optional)  │──► Payment, Notification, Analytics
             └────────────────────┘
```

**Runtime:** Spring Boot 3.2 (stateless) → MySQL 8 (primary + read replicas) → Redis (cache + seat hold) → Kafka (async fan-out). Deployed as containers on Kubernetes behind an ingress/ALB.

## 2. Logical Module Layout

```
controller/           REST edge
service/              Business logic (ShowBrowseService, BookingService, AdminShowService, OfferService)
service/discount/     DiscountEngine + RuleEvaluator strategies
repository/           Spring Data JPA
entity/               JPA entities (Booking, Show, ShowSeat, Offer, ...)
dto/                  Request/response contracts
exception/            GlobalExceptionHandler
```

See `SCHEMA.md` for the ER diagram.

## 3. Key Sequence Flows

### 3.1 Browse shows (READ)
```
Client → GET /api/v1/movies/{id}/shows?cityId=&date=
  └─► ShowController → ShowBrowseService
        └─► ShowRepository.findShowsByMovieCityAndDate()   (single JPQL, JOIN FETCH)
        └─► build grouped DTO (theatre → screens → timings)
  ◄── 200 JSON
```

### 3.2 Book tickets (WRITE)
```
Client → POST /api/v1/bookings {userId, showId, showSeatIds}
  └─► BookingController (validation)
        └─► BookingService.createBooking  [TX begin, READ_COMMITTED]
              ├─► userRepository.findById
              ├─► showSeatRepository.findAllByIdForUpdate  ← PESSIMISTIC_WRITE on rows
              ├─► assert all AVAILABLE and same show
              ├─► DiscountEngine.calculateDiscounts
              │     ├─► OfferRepository.findActiveOffers
              │     ├─► scope filter (city / theatre)
              │     ├─► RuleEvaluator.evaluate() per rule (Strategy)
              │     └─► compute discount per AppliesTo (PER_BOOKING / PER_TICKET / NTH_TICKET)
              ├─► persist Booking + BookingSeat(s) + BookingOffer(s)
              └─► mark ShowSeat.status = BOOKED  [TX commit]
  ◄── 201 BookingResponse   (or 409 on conflict / optimistic lock failure)
```

### 3.3 Cancel booking
```
DELETE /api/v1/bookings/{id}
  └─► BookingService.cancelBooking  [TX]
        ├─► pessimistic lock on booked ShowSeats
        ├─► release seats → AVAILABLE
        └─► booking.status = CANCELLED
```

### 3.4 Admin: create show
```
POST /api/v1/admin/shows
  └─► AdminShowService.createShow [TX]
        ├─► persist Show
        └─► auto-provision ShowSeat rows for every Seat on that Screen
              price = base × {REGULAR:1.0, PREMIUM:1.5, VIP:2.0}
```

## 4. Concurrency & Transactions

| Concern | Mechanism |
|---|---|
| Double booking | `SELECT ... FOR UPDATE` on `show_seat` rows (pessimistic row lock) |
| Stale reads in a booking flow | `@Transactional(isolation = READ_COMMITTED)` |
| Race past the pessimistic window | `@Version` on `ShowSeat` → `OptimisticLockingFailureException` → 409 |
| Atomicity | One `@Transactional` wraps booking + booking_seats + booking_offers + show_seat status |
| Admin delete guard | Refuses delete if `count(booking WHERE status in CONFIRMED,PENDING) > 0` |

**Why both locks?** Pessimistic prevents the common case (two clients selecting the same seats concurrently); `@Version` is a safety net against a classic TOCTOU window or if locking is ever downgraded.

## 5. Scaling Strategy

- **Stateless service** → horizontal scale behind an LB (Kubernetes HPA on CPU / RPS).
- **Read/write split** — route `GET` browse/offer queries to MySQL read replicas; writes hit the primary. Already decoupled at the service method level via `readOnly=true`.
- **Caching** — Redis for: (1) popular movie/show browse payloads, TTL ~30s; (2) active offers list; (3) short-lived "seat hold" (5–10 min TTL) before a user confirms payment.
- **Sharding** — when MySQL reaches write ceiling, shard by `city_id` (natural partition — cities rarely cross-boundary).
- **Hot show (blockbuster)** — queue requests at the gateway + pre-fragment seat inventory into cacheable blocks; consider event-sourced seat state for very hot shows.

## 6. Availability (target 99.99 %)

- **Multi-AZ** — service + DB + Redis replicas across ≥3 AZs.
- **DB failover** — MySQL primary with semi-sync replica + automated failover (MHA / RDS multi-AZ).
- **Circuit breakers** — Resilience4j around payment gateway and notification calls so downstream failure doesn't cascade.
- **Timeouts everywhere** — HTTP client 2s, DB query 5s, lock wait 3s.
- **Health & readiness probes** — `/actuator/health` drives Kubernetes liveness; readiness gates on DB connectivity.
- **Blue/green or canary deploys** — zero-downtime; Flyway migrations are additive only.

## 7. Payment Integration (design-only)

Interface + adapter pattern:

```java
interface PaymentGateway {
    PaymentIntent create(BookingId id, Money amount);
    PaymentStatus verify(String providerRef);
    Refund refund(String providerRef, Money amount);
}

// Implementations: RazorpayAdapter, StripeAdapter
```

Flow: client calls `POST /bookings` → service creates booking in `PENDING` + reserves seats → returns `PaymentIntent` → client pays → webhook hits `POST /payments/webhook` → we verify signature, set booking `CONFIRMED`, commit seats. A scheduled sweeper releases `PENDING` bookings older than 10 min.

## 8. Security (OWASP Top 10)

| Risk | Mitigation |
|---|---|
| A01 Broken access control | SSO/JWT at gateway; `@PreAuthorize` on admin endpoints; tenant/theatre scoping by claim |
| A02 Crypto failures | TLS 1.2+ everywhere; secrets in KMS/Vault, never in code |
| A03 Injection | JPA parameterized queries only; Jakarta Validation on all DTOs |
| A04 Insecure design | Threat-modelled seat-locking and payment flows; idempotency keys on booking |
| A05 Misconfig | No stacktrace in 5xx responses (GlobalExceptionHandler); Spring Actuator endpoints gated |
| A06 Vulnerable deps | Dependabot / `mvn dependency-check`, pinned base image |
| A07 AuthN failures | SSO-only; rotate JWT signing keys; rate-limit login at gateway |
| A08 Integrity failures | Signed container images; SBOM; webhook HMAC verification |
| A09 Logging/monitoring | Structured JSON logs, correlation IDs, centralized in ELK |
| A10 SSRF | Outbound egress locked down to payment gateway allowlist |

## 9. Monetization

- **Convenience fee** per ticket (absolute or %).
- **Platform fee** from theatre on each confirmed booking.
- **Sponsored placements** — movies/theatres boosted in browse results, labelled.
- **Premium subscription** — waived convenience fee, early booking window.
- **Ads** — pre-roll/banner on browse pages.

## 10. Integration with Existing Theatre IT

Two-tier strategy:

1. **Pull adapter** (for theatres with REST APIs) — scheduled poll every N minutes to sync screens/shows/seat inventory. Upserts keyed by `external_id`.
2. **Push adapter** (for theatres with messaging) — consume Kafka/AMQP from theatre; same idempotent upsert.
3. **Manual/file import** (long tail) — CSV upload via admin tool for theatres without systems.

Every integration sits behind a `TheatreIntegration` interface; one adapter per vendor.

## 11. Deployment

- **Local** — `docker-compose up` (MySQL + service).
- **Prod** — Kubernetes:
  - Service: `Deployment` (3+ replicas), `Service`, `HorizontalPodAutoscaler`.
  - DB: managed MySQL (RDS / Cloud SQL) multi-AZ.
  - Cache: managed Redis (ElastiCache / MemoryStore) cluster mode.
  - Ingress: ALB / NGINX with TLS termination.
  - Secrets: External Secrets Operator → KMS/Vault.
- **Migrations** — Flyway runs on app startup (single leader pod) OR as a pre-deploy Job.

## 12. Observability

- **Metrics** — Micrometer → Prometheus → Grafana dashboards (latency p50/p95/p99, RPS, error rate, booking conversion, lock-wait time).
- **Logs** — JSON logs, request/correlation IDs, shipped to ELK (Filebeat → Logstash → Elasticsearch → Kibana).
- **Tracing** — OpenTelemetry SDK → Jaeger/Tempo; trace ID propagated through gateway.
- **Alerting** — on error-rate spikes, lock timeouts, payment webhook failures, replica lag.

## 13. CI/CD

```
PR → GitHub Actions:
  build (mvn verify) → unit tests → OWASP dep-check → SonarQube → build image
  → trivy scan → push to registry → deploy to dev
Merge to main:
  → deploy to staging → smoke tests → manual approval → canary to prod (10% → 50% → 100%)
Rollback: Helm rollback on any SLO breach alert
```

## 14. Project Plan & Effort Estimate

| Phase | Scope | Effort |
|---|---|---|
| 1 | Project setup, Docker, config | 0.5 d |
| 2 | Schema, Flyway migrations, seed | 1 d |
| 3 | OpenAPI, contracts | 0.5 d |
| 4 | READ scenario (browse) | 1 d |
| 5 | WRITE scenario (booking + locking + offers) | 2 d |
| 6 | Admin CRUD + cancel | 0.5 d |
| 7 | Tests (unit + integration) | 1 d |
| 8 | NFR docs, HLD, sequence, deployment | 0.5 d |
| — | Buffer / review | 1 d |
| **Total** | | **≈ 8 d / 1 dev** |

## 15. Known Not-Implemented (design-only)

- Payment gateway adapter (interface shown)
- Seat-hold TTL via Redis (design shown)
- Auth/JWT (assumes SSO at gateway)
- Kafka-based async fan-out
- Redis caching layer

All are additive — they plug into the existing service without changing the booking contract.
