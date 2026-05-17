# ParkEase — Backend

Smart parking platform built using a **microservices architecture** with **Spring Boot 3**, **RabbitMQ**, **Redis**, and **PostgreSQL**.  
Designed for scalability, fault tolerance, and real-time parking workflows.

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3-green)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-3.12-ff6600)
![Docker](https://img.shields.io/badge/Docker-Enabled-blue)
![Render](https://img.shields.io/badge/Deploy-Render-purple)

# Architecture

ParkEase follows a **microservices architecture** with an **API Gateway** as the single entry point.

### Communication Style

- **Synchronous:** REST APIs
- **Asynchronous:** RabbitMQ events
- **Caching & Distributed Locks:** Redis + Redisson
- **Authentication:** JWT + Google OAuth2

```text
                        ┌─────────────────────────────────┐
                        │          API Gateway             │
                        │    Spring Cloud Gateway :8080    │
                        │  JWT validation · CORS · Routes  │
                        └──────────────┬──────────────────┘
                                       │
          ┌──────────┬─────────────────┼─────────────┬───────────┐
          │          │                 │             │           │
    ┌─────▼───┐ ┌────▼────┐ ┌─────────▼──┐ ┌───────▼──┐ ┌──────▼───────┐
    │  Auth   │ │ Parking │ │  Booking   │ │ Payment  │ │ Notification │
    │  :8081  │ │  :8082  │ │   :8083    │ │  :8084   │ │    :8085     │
    └─────────┘ └─────────┘ └────────────┘ └──────────┘ └──────────────┘
                                                              ┌───────────┐
                                                              │ Analytics │
                                                              │   :8086   │
                                                              └───────────┘

          └──────────────────── RabbitMQ (Events) ──────────────────────┘
          └──────────────────── PostgreSQL (Data) ───────────────────────┘
          └────────────────────── Redis (Cache) ─────────────────────────┘
```

---

# Microservices

| Service | Port | Responsibility |
|---|---|---|
| API Gateway | 8080 | JWT auth, routing, CORS, circuit breaker |
| Auth Service | 8081 | Registration, login, Google OAuth2, user management |
| Parking Service | 8082 | Parking lot CRUD, geo search, Redis availability counters |
| Booking Service | 8083 | Booking lifecycle, distributed locking, expiry scheduler |
| Payment Service | 8084 | Razorpay integration, payment verification |
| Notification Service | 8085 | In-app notifications, transactional emails |
| Analytics Service | 8086 | Occupancy, utilisation, traffic and revenue analytics |

---

# Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.3, Spring Cloud Gateway, Spring Security |
| Database | PostgreSQL 15 (JPA/Hibernate) |
| Cache | Redis 7 |
| Messaging | RabbitMQ 3.12 |
| Authentication | JWT (JJWT), Google OAuth2 |
| Distributed Locks | Redisson |
| Payments | Razorpay |
| Email | Resend |
| API Docs | SpringDoc OpenAPI |
| DevOps | Docker, Docker Compose, Render |

---

# Key Design Decisions

## Distributed Locking

Redisson Redis locks prevent double-booking of parking spots during concurrent requests.

---

## Event-Driven Architecture

Booking and payment events flow through RabbitMQ.

This keeps:
- notifications
- analytics
- payment workflows

fully decoupled from booking logic.

---

## Gateway-Level JWT Validation

JWT tokens are validated once at the API Gateway using:

```java
JwtAuthGlobalFilter
```

Downstream services trust forwarded headers:

```text
X-User-Id
X-User-Role
```

This avoids repeated token parsing in every service.

---

## Circuit Breaker

Analytics routes use Resilience4j circuit breakers with graceful fallbacks.

If analytics goes down:
- booking flow still works
- payment flow still works
- users are unaffected

---

## Role-Based Access Control

Supported roles:

- DRIVER
- MANAGER
- ADMIN

Security enforced at:
- gateway route level
- service method level using `@PreAuthorize`

---

# Running Locally

## Prerequisites

Install:

- Docker
- Docker Compose
- Java 17
- Maven

---

## Clone Repository

```bash
git clone https://github.com/Sandeep-cloud331/ParkEase.git
cd ParkEase
```

---

## Configure Environment Variables

Copy example env file:

```bash
cp .env.example .env
```

Fill all required values inside `.env`.

---

## Start All Services

```bash
docker-compose -f docker-compose.dev.yml up -d
```

---

## Local URLs

| Service | URL |
|---|---|
| API Gateway | http://localhost:8080 |
| Eureka Dashboard | http://localhost:8761 |
| Swagger UI | http://localhost:8080/swagger-ui.html |

---

# Environment Variables

| Variable | Description |
|---|---|
| GOOGLE_CLIENT_ID | Google OAuth2 Client ID |
| GOOGLE_CLIENT_SECRET | Google OAuth2 Client Secret |
| RAZORPAY_KEY_ID | Razorpay API Key |
| RAZORPAY_KEY_SECRET | Razorpay Secret |
| MAIL_USERNAME | SMTP Email |
| MAIL_PASSWORD | SMTP Password |
| JWT_SECRET | JWT Signing Secret |
| REDIS_URL | Redis Connection URL |
| RABBITMQ_URL | RabbitMQ Connection URL |

---

# API Overview

Base URL:

```text
https://parkease-api-gateway.onrender.com
```

Swagger Docs:

```text
/swagger-ui.html
```

---

# Authentication APIs

| Method | Endpoint | Access |
|---|---|---|
| POST | `/api/v1/auth/register` | Public |
| POST | `/api/v1/auth/login` | Public |
| GET | `/oauth2/authorization/google` | Public |
| POST | `/api/v1/auth/refresh` | Public |

---

# Parking APIs

| Method | Endpoint | Access |
|---|---|---|
| GET | `/api/v1/parking/lots/search` | Public |
| GET | `/api/v1/parking/lots/nearby` | Public |
| POST | `/api/v1/parking/manager/lots` | MANAGER |
| GET | `/api/v1/parking/admin/lots/pending` | ADMIN |

---

# Booking APIs

| Method | Endpoint | Access |
|---|---|---|
| POST | `/api/v1/bookings` | DRIVER |
| POST | `/api/v1/bookings/{id}/checkin` | DRIVER |
| POST | `/api/v1/bookings/{id}/checkout` | DRIVER |
| DELETE | `/api/v1/bookings/{id}` | DRIVER |

---

# Payment APIs

| Method | Endpoint | Access |
|---|---|---|
| POST | `/api/v1/payments/orders` | DRIVER |
| POST | `/api/v1/payments/verify` | DRIVER |

---

# Notification APIs

| Method | Endpoint | Access |
|---|---|---|
| GET | `/api/v1/notifications/recipient/{id}` | Authenticated |
| PATCH | `/api/v1/notifications/{id}/read` | Authenticated |

---

# Analytics APIs

| Method | Endpoint | Access |
|---|---|---|
| GET | `/api/v1/analytics/lots/{id}/occupancy` | MANAGER / ADMIN |
| GET | `/api/v1/analytics/lots/{id}/summary` | MANAGER / ADMIN |
| GET | `/api/v1/analytics/my` | DRIVER |

---

# Event Flow

```text
DRIVER books spot
      │
      ▼
Booking Service ──► booking.pending ──► Notification Service
      │
      ▼
Payment Service ──► payment.completed ──► Booking Service
                                     └──► Notification Service
                                     └──► Analytics Service
      │
      ▼
Booking Service ──► booking.confirmed ──► Notification Service
                                     └──► Analytics Service
```

---

# Deployment

Services are deployed individually on Render using Docker containers.

Each service uses:
- multi-stage Docker builds
- Maven build stage
- lightweight JRE runtime stage

---

# Production Differences

| Local | Production |
|---|---|
| Eureka enabled | Eureka disabled |
| Local Redis | Redis Cloud TLS |
| RabbitMQ local | RabbitMQ TLS (5671) |
| Local env files | Render environment variables |

---

# Project Features

## Authentication
- JWT Authentication
- Refresh Tokens
- Google OAuth2 Login
- Role-based Authorization

## Parking Management
- Parking lot CRUD
- Spot availability tracking
- Geo-based search
- Manager approval workflows

## Booking System
- Spot reservation
- Expiry scheduler
- Distributed locking
- Check-in / Check-out

## Payments
- Razorpay order creation
- Payment verification
- Event-driven confirmation

## Notifications
- In-app notifications
- Email notifications
- Event-based messaging

## Analytics
- Occupancy reports
- Revenue analytics
- Traffic analytics
- User booking insights

---

# Scalability Features

- Stateless microservices
- API Gateway routing
- Distributed locks
- Asynchronous messaging
- Redis caching
- Circuit breakers
- Independent deployments

---

# Future Improvements

- Kubernetes deployment
- Prometheus + Grafana monitoring
- Distributed tracing with Zipkin
- WebSocket live parking updates
- Rate limiting at gateway
- Multi-region deployment
- CI/CD pipelines
- ELK logging stack

---

# License

This project is licensed under the MIT License.
