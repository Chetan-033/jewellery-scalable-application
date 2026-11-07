# ADR-003: Offline Windows packaged application

**Date:** 2026-10-25  
**Status:** Accepted

## Context

Reference product can run from a local application for shops without reliable internet. That behaviour is required; PHP portable stack is not reused.

## Decision

Provide an **Electron launcher** under `desktop/` that:

1. Starts bundled/local MySQL (or uses installed MySQL).
2. Starts the Spring Boot jar with `offline` profile.
3. Opens the Angular UI in the app window.

Same backend/frontend artifacts as online; profile switches runtime.

## Consequences

- One codebase, two delivery channels.
- Packaging must ship JRE + jar + UI; DB may be bundled MariaDB later.
- Sync to cloud is a later optional feature, not required for v1 offline.
