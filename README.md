# Jewellery ERP

Online multi-tenant jewellery ERP for India: masters, inventory, Girvi loans, sales, credit, and platform admin for shop onboarding.

**Stack:** Java 21 · Spring Boot 3 · Angular · MySQL 8.4 · Flyway

## Repository layout

| Path | Purpose |
|------|---------|
| `backend/` | Spring Boot modular monolith |
| `frontend/` | Angular ERP UI + platform admin |
| `docs/` | PRD, ADRs, runbooks |
| `infra/` | Docker Compose MySQL (optional local) |
| `database/` | Seed notes |

## Prerequisites

- JDK 21
- Node.js 20+
- MySQL 8.4 (local install or Docker)

## Quick start (online)

1. Ensure MySQL is running and user/DB exist (see `.env.example`), or from `infra/`:
   `docker compose up -d mysql`

2. Backend:

```bash
cd backend
mvnw.cmd spring-boot:run
```

Default profile is **online**. Platform admin: `platform` / `Platform@123`.

3. Frontend:

```bash
cd frontend
npm ci
npm start
```

- Shop login: http://localhost:4200/login (shop code + owner user)
- Platform admin: http://localhost:4200/platform/login

API: http://localhost:18080

## Profiles

| Profile | Use |
|---------|-----|
| `online` (default) | Platform DB + database-per-tenant |
| `test` | In-memory H2 for automated tests |

## Documentation

Start at [docs/PRD.md](docs/PRD.md) and [docs/adr/ADR-002-database-per-tenant.md](docs/adr/ADR-002-database-per-tenant.md).

## License

Proprietary — all rights reserved.
