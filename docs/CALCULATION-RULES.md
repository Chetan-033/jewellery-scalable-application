# Calculation Rules

**Status:** Provisional — business owner sign-off required before production invoice/loan use.

## Standards

- Use `BigDecimal` only (`MoneyMath` + `MetalValuationEngine`).
- Scales: money 2, rate/weight/purity 4, rounding `HALF_UP`.
- Store inputs + formula notes on finalized sales invoices (`calc_snapshot`).

## Metal line (aligned to reference Omunim fields)

Inputs: gross weight, stone weight, net weight, purity %, wastage %, rate/g, making type/rate, stone amount, discount, CGST/SGST/IGST %.

```
netWeight     = grossWeight - stoneWeight   (if net not supplied)
fineWeight    = netWeight * (purity / 100)
finalFine     = netWeight * ((purity + wastage) / 100)   // WASTAGE_MODE=ADD_TO_PURITY
metalAmount   = finalFine * ratePerGram
makingAmount  =
  PER_GRAM -> netWeight * makingRate * qty
  PERCENT  -> metalAmount * makingRate / 100
  FIXED    -> makingRate * qty
taxable       = metalAmount + makingAmount + stoneAmount - discount
tax           = taxable * (cgst% + sgst% + igst%) / 100
lineTotal     = taxable + tax
```

Default sell tax preview: CGST 1.5% + SGST 1.5% (configurable per line).

## Sign-off

| Rule set | Owner | Date | Signed |
|----------|-------|------|--------|
| Metal valuation | | | No |
| Old metal modes | | | No |
| Interest engines | | | No |
