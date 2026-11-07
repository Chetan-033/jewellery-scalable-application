# Jewellery ERP — Offline Desktop

Electron launcher that starts the local Spring Boot API (`offline` profile) and opens the Angular UI — matching the reference behaviour of “run the application file and work offline”.

## Dev start

1. Build backend jar: `cd backend && mvnw.cmd -DskipTests package`
2. Ensure MySQL has `jewellery_erp_tenant_demo` (or set `MYSQL_*`).
3. Optionally build UI: `cd frontend && npm run build:offline`
4. `cd desktop && npm ci && npm start`

If UI dist is missing, the launcher opens `http://localhost:4200` (run `npm start` in `frontend`).

## Packaging resources

Place before `npm run dist`:

- `resources/backend/jewellery-erp.jar`
- `resources/frontend/` = contents of Angular `dist/jewellery-erp-ui`

Bundled JRE / MariaDB can be added under `resources/runtime` in a later packaging pass.

## First-run bootstrap

Backend `offline` profile creates admin user from `jewellery.bootstrap.*` (default `admin` / `Admin@123`). Change immediately after first login.
