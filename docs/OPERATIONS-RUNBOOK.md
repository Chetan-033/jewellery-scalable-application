# Operations Runbook (draft)

## Offline shop machine

1. Install MySQL 8.4 (or use bundled MariaDB when packaging includes it).
2. Create DB `jewellery_erp_tenant_demo`.
3. Install / run Jewellery ERP desktop app.
4. First login: change `admin` password immediately.
5. Configure firm, metals, rates, then open stock.

## Backup (offline)

- Dump MySQL tenant DB daily.
- Copy attachment/file store directory.
- Test restore monthly onto a spare PC.

## Online tenant ops

- Provision via platform registry (database-per-tenant).
- Backup each tenant DB + object storage prefix.
- Migration: backup → restore to new placement → validate balances → cutover registry → monitor.

## Support access

Time-limited, audited. Never bypass tenant isolation.
