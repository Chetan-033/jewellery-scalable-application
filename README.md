# Jewellery ERP

Modern jewellery business platform for India: multi-branch retail/wholesale, inventory, Girvi loans, job work, accounting, GST-oriented reporting, with **online multi-tenant** and **offline Windows packaged** editions.

**Stack:** Java 21 · Spring Boot 3 · Angular · MySQL 8.4 · Flyway · Electron (offline launcher)

Reference PHP systems are used only as a **behaviour checklist**. This codebase does not wrap or reuse legacy APIs.

## Repository layout

| Path | Purpose |
|------|---------|
| `backend/` | Spring Boot modular monolith |
| `frontend/` | Angular ERP UI |
| `desktop/` | Offline Windows launcher (Electron) |
| `docs/` | PRD, functional map, ADRs, calculation rules, runbooks |
| `infra/` | Docker Compose, MySQL init |
| `database/` | Seed data |
| `tests/` | Cross-cutting fixtures and e2e notes |
| `.github/workflows/` | CI |

## Prerequisites

- JDK 21 (Temurin recommended)
- Node.js 20+ (LTS)
- MySQL 8.4 (local service or Docker)
- Maven Wrapper is included (`backend/mvnw`)

## Quick start (local / offline profile)

Offline profile uses a **local H2 file database** under `backend/data/` by default (no MySQL password required). Override with `OFFLINE_JDBC_URL` / MySQL for production-like shops.

1. Backend:

```bash
cd backend
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=offline
```

2. Frontend:

```bash
cd frontend
npm ci
npm start
```

Open http://localhost:4200 — API listens on **http://localhost:18080** by default (avoids legacy PHP on 8080). First boot creates admin `admin` / `Admin@123` (change immediately).

3. Offline desktop (starts API jar + UI window):

```bash
cd backend && mvnw.cmd -DskipTests package
cd ../desktop && npm ci && npm start
```

Optional MySQL (online / shop prefer MySQL): copy `.env.example`, create DBs via `infra/mysql/init/01-databases.sql` or Docker Compose, set `OFFLINE_JDBC_URL` to a MySQL JDBC URL.

## Profiles

| Profile | Use |
|---------|-----|
| `offline` | Single local tenant DB, local files, desktop app |
| `online` | Platform DB + database-per-tenant placement |

## Documentation

Start at [docs/PRD.md](docs/PRD.md) and [docs/FUNCTIONAL-MAP.md](docs/FUNCTIONAL-MAP.md).

## License

Proprietary — all rights reserved.


