# Deployment Runbook (draft)

## Local offline API

```bash
cd backend
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=offline
```

API default port: **18080** (override with `SERVER_PORT`).  
Offline DB default: H2 file under `backend/data/`.  
Default admin after bootstrap: `admin` / `Admin@123` (change immediately).

## Local UI

```bash
cd frontend
npm ci
npm start
```

UI calls `http://localhost:18080/api/v1`.

## Offline desktop

```bash
cd backend && mvnw.cmd -DskipTests package
cd ../frontend && npm run build
cd ../desktop && npm ci && npm start
```

Launcher starts the jar on port 18080 and opens the UI window.

## Online note

Use profile `online` with platform MySQL registry and database-per-tenant placement (router to be expanded in later slices).

## CI artifacts

GitHub Actions builds backend jar and frontend `dist`, then stages offline packaging resources on Windows.
