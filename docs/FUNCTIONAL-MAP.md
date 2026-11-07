# Functional Map

Legacy PHP (Omunim) is a **behaviour checklist only**. Status: `Preserve` = must exist; `Improve` = redesign; `New` = modern capability; `Optional` = later phase; `Scaffolded` = package/route exists.

## Platform & tenant

| Capability | Status | Notes |
|------------|--------|-------|
| Tenant create/suspend/reactivate | Preserve | Online platform registry |
| Plans / feature entitlements / quotas | Preserve | Server-side enforce |
| Tenant onboarding wizard | Improve | Offline first-run + online wizard |
| Backup / restore / migration jobs | Preserve | Offline local + online ops |
| Offline packaged application | Preserve | Electron launcher (not PHP portable) |

## Organization & users

| Capability | Status |
|------------|--------|
| Firm / branch / counter / warehouse | Preserve |
| Users, roles, permissions, branch scope | Preserve |
| MFA / session revoke | Improve |
| Staff attendance / payroll | Optional |

## Masters & rates

| Capability | Status | Vertical slice |
|------------|--------|----------------|
| Customer / supplier / karigar | Preserve | Customer done |
| Metal / purity (tunch) | Preserve | Done |
| Product / stone / HSN / tax category | Preserve | Scaffolded |
| Daily metal / crystal / making / wastage rates | Preserve | Metal rate done |
| Number series | Preserve | Pending |

## Inventory

| Capability | Status | Vertical slice |
|------------|--------|----------------|
| Stock item / lot / tag | Preserve | Item done |
| Stock movements ledger | Preserve | IN/OUT done |
| Transfer / tally / barcode / hallmark | Preserve | Pending |
| Opening import Excel/CSV | Preserve | Pending |

## Sales / purchase / old metal

| Capability | Status |
|------------|--------|
| POS / invoice / 10 layout templates / 1 calc engine | Preserve |
| Estimate → order → invoice | Preserve |
| Returns / exchanges | Preserve |
| Purchase PO / GRN / invoice / return | Preserve |
| Old metal / URD / exchange | Preserve |

## Credit & Girvi

| Capability | Status |
|------------|--------|
| Udhaar / advances / metal advance / deposits | Preserve |
| Pledge loan / interest engines / packet custody / release | Preserve |
| Form 7/8 layouts (jurisdiction verified later) | Preserve |

## Manufacturing & engagement

| Capability | Status |
|------------|--------|
| Karigar job / WIP / wastage variance | Preserve |
| Melting / refining / assay | Preserve |
| Repair / customer order / schemes | Preserve |
| Messaging (SMS/WhatsApp/email) | Improve |
| RFID / e-comm / digital gold | Optional |

## Accounting & GST

| Capability | Status |
|------------|--------|
| COA / journals / Day Book / TB / P&L / BS | Preserve |
| Auto-posting from finalized docs | Preserve |
| GST reports / HSN; e-invoice when applicable | Preserve (sign-off required) |

## Reference PHP surface (checklist pointers)

Online/offline API models observed for behaviour inventory (not for reuse): `stock`, `loan`, `invoice`, `melting`, `gst`, `scheme`, `repair`, `journal`, `rates`, `master`, `firm`, `user`, `transaction`, `attendance`, `salary`, WhatsApp/Telegram messaging, RFID visitor tools.

Prefix clusters in offline UI (`2/include/php`): sales (`ogij`/`ogsp`), Girvi (`olgg`), stock (`ogrw`/`ogrm`), crystal (`ogcr`), schemes (`omkt`/`omsc`), accounting (`omac`), loans (`omlo`), wholesale (`ogwh`), barcode/bill (`ogbb`).
