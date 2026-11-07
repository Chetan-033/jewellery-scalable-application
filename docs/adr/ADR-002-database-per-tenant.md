# ADR-002: Database-per-tenant (online)

**Date:** 2026-10-25  
**Status:** Accepted

## Context

Jewellery data is sensitive; backup/migrate/export must be clean. Shared-schema filtering is error-prone.

## Decision

Online edition uses a **platform registry DB** plus **one MySQL database (or schema) per tenant**. Offline edition uses a single local tenant database with bootstrap.

## Consequences

- Stronger isolation and simpler tenant backup/restore/migration.
- Higher ops cost at large tenant counts (acceptable for v1).
- Tenant routing will be added on the online profile; offline needs no router.
