# ClimateGuard API

A production-ready REST API for climate observations and flood risk data,
seeded with real September 2024 Central European Flood data from Polish regions.

## Stack

Java 17 · Spring Boot 3.2 · PostgreSQL · Spring Security (JWT) · Flyway · MapStruct · Docker

## Quick Start

### With Docker (recommended)
```bash
cp .env.example .env
docker-compose up --build
```

### Local Development
```bash
# Start PostgreSQL
docker run --name pg-climateguard \
  -e POSTGRES_PASSWORD=password \
  -e POSTGRES_DB=climateguard \
  -p 5432:5432 -d postgres:15

# Run app
mvn spring-boot:run
```

## API Docs
Open http://localhost:8080/swagger-ui.html

## Default Credentials
- **Admin**: `admin` / `admin123`

## Key Endpoints

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | /api/v1/auth/login | Public | Get JWT token |
| GET | /api/v1/regions | USER | List all regions |
| GET | /api/v1/observations/region/{id}/stats | USER | Climate stats |
| GET | /api/v1/flood-events/high-risk | USER | High risk events |
| POST | /api/v1/regions | ADMIN | Create region |

## Architecture

```
Controller → Service → Repository → PostgreSQL
```

Security: JWT Bearer token, role-based access (USER / ADMIN)

Database migrations managed by Flyway (V1–V5).
