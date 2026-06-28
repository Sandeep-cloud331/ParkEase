# ParkEase — Parking Lot Management System

A microservices-based backend system for parking lot discovery, real-time slot booking, payment processing, and analytics. Built with Spring Boot and Spring Cloud.

---

## Architecture

```
Client
  └── API Gateway (8080)  ←  JWT validation, routing, circuit breaker
        ├── Auth Service       (8081)  ←  JWT, OAuth2 (Google), RBAC
        ├── Parking Service    (8082)  ←  Lots, spots, geo-search, availability
        ├── Booking Service    (8083)  ←  Booking lifecycle, distributed locks
        ├── Payment Service    (8084)  ←  Razorpay integration, PDF invoices
        ├── Notification Svc   (8085)  ←  Email via RabbitMQ events
        └── Analytics Service  (8086)  ←  Utilization & revenue reporting

Service Registry: Eureka Server (8761)
Infrastructure:   PostgreSQL · Redis · RabbitMQ
```

All inter-service communication goes through Eureka discovery with Spring Cloud Load Balancer.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.3.4, Spring Cloud 2023.0.3 |
| API Gateway | Spring Cloud Gateway (WebFlux) |
| Service Discovery | Netflix Eureka |
| Security | Spring Security, JWT (JJWT 0.12.5), OAuth2 (Google) |
| Database | PostgreSQL 16 (separate DB per service) |
| Cache / Locks | Redis 7, Redisson 3.27.2 |
| Messaging | RabbitMQ 3.12, Spring AMQP |
| Payments | Razorpay Java SDK 1.4.6 |
| PDF Generation | iText 5.5.13.3 |
| Resilience | Resilience4j (circuit breaker on analytics) |
| API Docs | Springdoc OpenAPI / Swagger UI |
| Build | Maven, Docker, Docker Compose |

---

## Key Features

- **Booking with distributed locking** — Redisson locks prevent double-booking under concurrent requests (10s timeout)
- **Payment flow** — Razorpay order creation, webhook verification, and PDF invoice generation
- **Event-driven updates** — RabbitMQ propagates booking and payment events to parking, notification, and analytics services
- **Google OAuth2** — Social login alongside standard JWT authentication
- **Auto-cancellation** — Bookings without check-in are cancelled after a 15-minute grace period
- **Analytics** — Per-lot occupancy, revenue, and booking trend reports via WebClient aggregation

---

## Running Locally

### Prerequisites
- Docker and Docker Compose
- JDK 17 (for local IDE runs only)

### 1. Configure environment

```bash
cp .env.example .env
```

Edit `.env` with your credentials (see [Environment Variables](#environment-variables)).

### 2. Start infrastructure + services

```bash
docker compose up -d
```

This starts PostgreSQL, Redis, RabbitMQ, and all 7 application services.

### 3. Verify

- Eureka dashboard: http://localhost:8761
- API Gateway: http://localhost:8080
- Swagger UI (example): http://localhost:8081/swagger-ui.html

> Each service exposes its own Swagger UI on its port (8081–8086).

---

## Service Port Reference

| Service | Port | Swagger UI |
|---|---|---|
| Eureka Server | 8761 | http://localhost:8761 |
| API Gateway | 8080 | — |
| Auth Service | 8081 | http://localhost:8081/swagger-ui.html |
| Parking Service | 8082 | http://localhost:8082/swagger-ui.html |
| Booking Service | 8083 | http://localhost:8083/swagger-ui.html |
| Payment Service | 8084 | http://localhost:8084/swagger-ui.html |
| Notification Service | 8085 | http://localhost:8085/swagger-ui.html |
| Analytics Service | 8086 | http://localhost:8086/swagger-ui.html |

Infrastructure: PostgreSQL `5432` · Redis `6379` · RabbitMQ `5672` (management: `15672`)

---

## Environment Variables

Copy `.env.example` to `.env` and fill in the values below. Docker Compose and Spring services both read from this file.

| Variable | Description |
|---|---|
| `POSTGRES_USER` | PostgreSQL username |
| `POSTGRES_PASSWORD` | PostgreSQL password |
| `REDIS_PASSWORD` | Redis auth password |
| `RABBITMQ_USER` | RabbitMQ username |
| `RABBITMQ_PASSWORD` | RabbitMQ password |
| `JWT_SECRET` | HS256 signing secret (64+ chars) |
| `JWT_EXPIRY_MS` | Access token TTL in ms (e.g. `86400000` = 24h) |
| `JWT_REFRESH_EXPIRY_MS` | Refresh token TTL in ms (e.g. `604800000` = 7d) |
| `GOOGLE_CLIENT_ID` | Google OAuth2 client ID |
| `GOOGLE_CLIENT_SECRET` | Google OAuth2 client secret |
| `RAZORPAY_KEY_ID` | Razorpay API key ID |
| `RAZORPAY_KEY_SECRET` | Razorpay API key secret |
| `MAIL_USERNAME` | Gmail account for notifications |
| `MAIL_PASSWORD` | Gmail app password |
| `MAIL_FROM` | Sender address for notification emails |

---

## Database

Each service owns its own PostgreSQL database. Hibernate manages schema via `ddl-auto: update`.

| Service | Database |
|---|---|
| Auth | `auth_db` |
| Parking | `parking_db` |
| Booking | `booking_db` |
| Payment | `payment_db` |
| Notification | `notification_db` |
| Analytics | `analytics_db` |

---

## Development Build

To build and run a single service locally without Docker:

```bash
cd booking-service
mvn spring-boot:run
```

Ensure the `.env` file exists at the repo root — services load it automatically via Spring Dotenv.

To build all services and run tests:

```bash
mvn clean verify
```
