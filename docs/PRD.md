# Jewellery ERP — Product Requirements (PRD)

**Version:** 1.0 planning baseline  
**Stack:** Java 21 + Spring Boot 3 + Angular + MySQL 8.4  
**Market:** India (retail / wholesale jewellery)

## Vision

A reliable, modern, configurable jewellery platform for one shop or many branches: metals, retail/wholesale, credit, Girvi loans, job work, accounting, statutory reports, online multi-tenant hosting, and offline Windows packaging.

## Principles

1. One source of truth per business fact (invoice engine, metal calc, stock ledger, accounting posting, permissions).
2. Configuration over hard-coding; every setting validated, permissioned, audited.
3. Tenant isolation by design (online: database-per-tenant).
4. Immutable finalized financial documents; corrections via controlled workflows.
5. Traceability: every movement links to a source transaction.
6. `BigDecimal` only for money, rates, weights, purity, interest.
7. Deploy vertical slices; modular monolith first.

## Editions

| Edition | Audience | Runtime |
|---------|----------|---------|
| Online | Hosted multi-tenant | Browser → API → platform DB + tenant DBs |
| Offline | Shop PCs without cloud | Windows app launches local DB + API + UI |

## Success / Definition of done (product)

- Business owner signs workflows, formulas, rounding, reports, invoice samples.
- Automated tests cover calculations, accounting balance, stock conservation, tenant isolation, permissions, migration.
- CI builds FE/BE, tests, scans, versioned artifacts.
- Production: TLS, monitoring, backup, tested restore, rollback, tenant onboarding.
- Offline: double-click app starts usable ERP locally.

## Out of scope for this PRD revision

Copying PHP structure/APIs. Legal/statutory claims without accountant sign-off. Microservices split before domain maturity.
