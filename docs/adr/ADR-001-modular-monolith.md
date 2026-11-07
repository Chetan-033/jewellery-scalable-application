# ADR-001: Modular monolith first

**Date:** 2026-10-25  
**Status:** Accepted

## Context

The product covers many jewellery domains. Microservices too early create distributed transactions and ops overhead.

## Decision

Ship a Spring Boot **modular monolith** with clear packages per domain. Split services only when scaling, ownership, or release cadence require it.

## Consequences

- Single deployable for online API and offline package.
- Local transactions for finalize invoice + stock + journal.
- Future extraction uses DTO/API boundaries already enforced.
