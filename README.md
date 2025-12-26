# PulseForge

Production-grade, multi-tenant event ingestion and analytics platform.

## Architecture

### System Overview

```
┌─────────────┐
│   Clients   │
└──────┬──────┘
       │ HTTP (JWT/API Key)
       ▼
┌─────────────────────────────────────────┐
│         Spring Boot Application         │
│  ┌────────────┐      ┌───────────────┐ │
│  │ Ingestion  │──┬──▶│   RabbitMQ    │ │
│  │    API     │  │   │   Publisher   │ │
│  └────────────┘  │   └───────────────┘ │
│                  │                      │
│  ┌────────────┐  │   ┌───────────────┐ │
│  │ Analytics  │  └──▶│     Redis     │ │
│  │    API     │      │  (Cache/Rate) │ │
│  └────────────┘      └───────────────┘ │
└─────────────────────────────────────────┘
       │                      ▲
       │ Async                │ Consume
       ▼                      │
┌──────────────┐      ┌───────────────┐
│   RabbitMQ   │─────▶│    Worker     │
│    Queue     │      │   Service     │
└──────────────┘      └───────┬───────┘
                              │ Persist
                              ▼
                      ┌───────────────┐
                      │  PostgreSQL   │
                      │ (Events/Aggs) │
                      └───────────────┘
```

### Data Flow

**Ingestion Path:**
1. Client sends event with API key → `POST /v1/ingest`
2. Controller validates, publishes to RabbitMQ → `202 Accepted`
3. Worker consumes, checks idempotency (Redis), persists to PostgreSQL
4. Scheduled job aggregates raw events into hourly rollups

**Analytics Path:**
1. Client queries with JWT → `GET /analytics`
2. Check Redis cache → hit: return immediately
3. Cache miss → query PostgreSQL aggregations → populate cache → return

### Multi-Tenancy

- **Workspace**: Tenant boundary (isolated data, users, projects)
- **Row-level filtering**: All queries filtered by `workspace_id`
- **RBAC**: Membership entity links users to workspaces with roles
- **Security**: Extract workspace from JWT/API key, enforce at repository layer

### Key Components

| Layer | Responsibility | Technologies |
|-------|---------------|--------------|
| **API** | HTTP endpoints, validation, auth | Spring MVC, Spring Security |
| **Service** | Business logic, orchestration | Spring transactions, caching |
| **Repository** | Data access, tenant filtering | Spring Data JPA, Flyway |
| **Messaging** | Async processing, reliability | RabbitMQ, manual acks, DLQ |
| **Caching** | Performance, deduplication | Redis, Lettuce, cache-aside |
| **Observability** | Metrics, logs, traces | Actuator, Prometheus, SLF4J |

### Reliability Patterns

- **Idempotency**: Redis deduplication + DB unique constraint
- **Retry logic**: Exponential backoff with `@Retryable`
- **Dead letter queue**: Failed messages routed to DLQ for replay
- **Manual acks**: Messages only removed after successful processing
- **Connection pooling**: HikariCP (PostgreSQL), Lettuce (Redis)

### Performance Optimizations

- **Batch processing**: Hibernate batch inserts (size: 20)
- **Query caching**: Redis with TTL-based invalidation
- **Async ingestion**: Decouple write path from processing
- **Aggregations**: Pre-computed hourly rollups for fast queries
- **Rate limiting**: Token bucket per tenant (Redis-backed)

## Tech Stack

- **Runtime**: Java 25, Spring Boot 4.0
- **Database**: PostgreSQL 16 (HikariCP pooling)
- **Cache**: Redis 7 (Lettuce client)
- **Messaging**: RabbitMQ 3.13
- **Observability**: Micrometer, Prometheus, Grafana
- **Testing**: JUnit 5, Testcontainers, k6

## Quick Start

### Prerequisites
- Java 25
- Docker & Docker Compose

### Run Infrastructure
```bash
docker-compose up -d
```

### Run Application
```bash
./mvnw spring-boot:run
```

### Verify
```bash
curl http://localhost:8080/actuator/health
```

## Project Structure

```
src/main/java/com/akarengin/pulseforge/
├── controller/     # REST endpoints
├── service/        # Business logic
├── repository/     # Data access
├── entity/         # JPA entities
├── config/         # Spring configuration
├── security/       # Auth filters
└── messaging/      # RabbitMQ consumers

src/main/resources/
├── application.yml         # Configuration
└── db/migration/          # Flyway migrations
```

## Development

### Build
```bash
./mvnw clean package
```

### Test
```bash
./mvnw test
```

### Database Migrations
```bash
./mvnw flyway:migrate
```

## Design Decisions

### Why RabbitMQ?
- Decouples ingestion from processing (handles traffic spikes)
- Guaranteed delivery with manual acknowledgment
- Dead letter queue for failure handling

### Why Redis?
- Sub-millisecond API key lookups (high read, low write)
- Distributed idempotency checks across instances
- Token bucket rate limiting with atomic operations

### Why Flyway?
- Version-controlled schema changes
- Prevents drift between environments
- Explicit migrations (no auto-DDL in production)

### Why Hibernate `validate`?
- Production-safe (won't modify schema)
- Forces explicit Flyway migrations
- Catches entity/schema mismatches at startup

## Scalability

- **Horizontal**: Stateless app instances behind load balancer
- **Database**: Read replicas for analytics, write to primary
- **Redis**: Cluster mode for distributed caching
- **RabbitMQ**: Clustered with mirrored queues
- **Workers**: Scale independently based on queue depth

## Observability

- **Metrics**: `/actuator/prometheus` (ingestion rate, latency, errors)
- **Health**: `/actuator/health` (DB, Redis, RabbitMQ connectivity)
- **Logs**: Structured JSON with correlation IDs and tenant context
- **Tracing**: OpenTelemetry spans across ingestion flow

## Security

- **Authentication**: JWT (user APIs), API keys (ingestion)
- **Authorization**: RBAC via workspace membership
- **Secrets**: BCrypt hashing for passwords and API keys
- **Validation**: Bean Validation on all inputs
- **Rate limiting**: Per-tenant token bucket
