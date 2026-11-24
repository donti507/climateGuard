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

Requires **Java 17+** and **Maven 3.8+**.

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

## Endpoints

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | /api/v1/auth/register | Public | Register a new user |
| POST | /api/v1/auth/login | Public | Login and receive JWT token |
| GET | /api/v1/regions | USER | List all regions (paginated, filterable by country) |
| GET | /api/v1/regions/{id} | USER | Get a region by ID |
| POST | /api/v1/regions | ADMIN | Create a new region |
| PUT | /api/v1/regions/{id} | ADMIN | Update a region |
| DELETE | /api/v1/regions/{id} | ADMIN | Delete a region |
| GET | /api/v1/observations | USER | List all observations (paginated) |
| GET | /api/v1/observations/{id} | USER | Get an observation by ID |
| GET | /api/v1/observations/region/{regionId} | USER | Get observations for a region with optional filters |
| GET | /api/v1/observations/region/{regionId}/stats | USER | Get climate statistics for a region |
| POST | /api/v1/observations | ADMIN | Add a new observation |
| DELETE | /api/v1/observations/{id} | ADMIN | Delete an observation |
| GET | /api/v1/flood-events | USER | List all flood events (paginated, filterable) |
| GET | /api/v1/flood-events/{id} | USER | Get a flood event by ID |
| GET | /api/v1/flood-events/region/{regionId} | USER | Get all flood events for a region |
| GET | /api/v1/flood-events/high-risk | USER | Get all events with risk score > 0.7 |
| POST | /api/v1/flood-events | ADMIN | Create a flood event |
| PUT | /api/v1/flood-events/{id} | ADMIN | Update a flood event |
| DELETE | /api/v1/flood-events/{id} | ADMIN | Delete a flood event |

## Architecture

```
Controller → Service → Repository → PostgreSQL
```

Security: JWT Bearer token, role-based access (USER / ADMIN)

Database migrations managed by Flyway (V1–V5).
