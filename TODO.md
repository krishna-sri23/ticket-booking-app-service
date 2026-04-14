# Movie Ticket Booking Platform - TODO

## Tech Stack
- **Backend:** Java 17 + Spring Boot 3.x
- **Database:** MySQL 8
- **Containerization:** Docker + Docker Compose
- **Build:** Maven
- **Code Repo:** `ticket-booking-app-service/`

---

## Phase 1: Project Setup
- [ ] Initialize Spring Boot project (Maven, Java 17)
- [ ] Add dependencies: Spring Web, Spring Data JPA, MySQL Driver, Lombok, Validation, Flyway
- [ ] Create `docker-compose.yml` with MySQL 8 container
- [ ] Configure `application.yml` (datasource, JPA, Flyway)
- [ ] Verify app starts and connects to MySQL via Docker

## Phase 2: Database Schema & Data Model
- [ ] Design ER diagram
- [ ] Create Flyway migration scripts for all tables:
  - [ ] `city` — id, name, state, country
  - [ ] `theatre` — id, name, address, city_id, total_screens
  - [ ] `screen` — id, theatre_id, name, total_seats
  - [ ] `seat` — id, screen_id, seat_number, seat_type (REGULAR/PREMIUM/VIP), row_label
  - [ ] `movie` — id, title, language, genre, duration_minutes, release_date, rating
  - [ ] `show` — id, movie_id, screen_id, show_date, start_time, end_time, price
  - [ ] `show_seat` — id, show_id, seat_id, status (AVAILABLE/BOOKED/BLOCKED), price
  - [ ] `booking` — id, user_id, show_id, total_amount, discount, final_amount, status, booked_at
  - [ ] `booking_seat` — id, booking_id, show_seat_id
  - [ ] `user` — id, name, email, phone, role (CUSTOMER/THEATRE_ADMIN)
  - [ ] `offer` — id, city_id, theatre_id, description, discount_type, discount_value, conditions
- [ ] Seed sample data (cities, theatres, movies, shows)

## Phase 3: API Contracts (OpenAPI/Swagger)
- [ ] Add Springdoc OpenAPI dependency
- [ ] Define API contracts:
  - [ ] `GET /api/v1/movies` — list movies by city/language/genre
  - [ ] `GET /api/v1/movies/{movieId}/shows` — theatres & shows for a movie in a city by date
  - [ ] `GET /api/v1/shows/{showId}/seats` — seat availability for a show
  - [ ] `GET /api/v1/offers?cityId=&theatreId=` — available offers
  - [ ] `POST /api/v1/bookings` — book tickets
  - [ ] `DELETE /api/v1/bookings/{bookingId}` — cancel booking
  - [ ] `POST /api/v1/admin/shows` — create show (theatre admin)
  - [ ] `PUT /api/v1/admin/shows/{showId}` — update show
  - [ ] `DELETE /api/v1/admin/shows/{showId}` — delete show
  - [ ] `PUT /api/v1/admin/shows/{showId}/seats` — allocate/update seat inventory

## Phase 4: READ Scenario Implementation (Mandatory)
**Browse theatres running a movie in a city with show timings by date**
- [ ] Entity classes: City, Theatre, Screen, Movie, Show, Seat, ShowSeat
- [ ] Repository layer
- [ ] Service layer with business logic
- [ ] Controller: `GET /api/v1/movies/{movieId}/shows?cityId=1&date=2026-04-15`
- [ ] Response DTO: grouped by theatre -> screens -> show timings
- [ ] Offer/discount calculation logic:
  - 50% off on 3rd ticket
  - 20% off for afternoon shows (12:00-17:00)
- [ ] Unit tests

## Phase 5: WRITE Scenario Implementation (Good to Have)
**Book movie tickets by selecting theatre, timing, and seats**
- [ ] Booking request DTO (showId, seatIds, userId)
- [ ] Seat locking mechanism (optimistic locking / pessimistic locking)
- [ ] Discount/offer application during booking
- [ ] Transaction management (@Transactional)
- [ ] Booking confirmation response
- [ ] Concurrent booking handling (prevent double booking)
- [ ] Unit tests

**Theatre admin: CRUD shows**
- [ ] Create show (movie, screen, date, time, pricing)
- [ ] Update show details
- [ ] Delete show (only if no bookings)
- [ ] Seat inventory allocation for a show

## Phase 6: Design Patterns & Best Practices
- [ ] Strategy pattern for discount/offer calculation
- [ ] Builder pattern for booking creation
- [ ] Repository pattern (Spring Data JPA)
- [ ] DTO pattern (separate request/response from entities)
- [ ] Global exception handling (@ControllerAdvice)
- [ ] Input validation (Jakarta Bean Validation)
- [ ] Proper HTTP status codes

## Phase 7: Non-Functional Requirements (Design/Documentation)
- [ ] Transaction management strategy (document in README)
- [ ] Scaling strategy: horizontal scaling, DB read replicas, caching (Redis)
- [ ] 99.99% availability: multi-AZ deployment, load balancing, circuit breakers
- [ ] Payment gateway integration design (Razorpay/Stripe — interface + adapter)
- [ ] OWASP Top 10 mitigation strategies
- [ ] Monetization model (platform fee, convenience fee, ads)
- [ ] Integration strategy for existing theatre IT systems (REST APIs, message queues)

## Phase 8: Architecture Artifacts
- [ ] High-Level Architecture Diagram (HLD)
- [ ] Database ER Diagram
- [ ] API sequence diagrams (browse + book flows)
- [ ] Deployment architecture (Docker, Kubernetes, AWS/GCP)
- [ ] Monitoring strategy (ELK, Prometheus + Grafana)
- [ ] CI/CD pipeline design
- [ ] Project plan & effort estimation

---

## Priority Order
1. Project setup + Docker (Phase 1)
2. DB schema + seed data (Phase 2)
3. READ scenario — browse shows (Phase 4)
4. API contracts/Swagger (Phase 3)
5. WRITE scenario — booking (Phase 5)
6. Design patterns cleanup (Phase 6)
7. Architecture docs (Phase 7 + 8)
