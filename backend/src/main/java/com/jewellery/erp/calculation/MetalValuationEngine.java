package com.jewellery.erp.calculation;

import com.jewellery.erp.shared.domain.MoneyMath;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Jewellery line valuation aligned to common Omunim-style fields:
 * gross/net/fine, purity + wastage, metal rate, making (PER_GRAM / PERCENT / FIXED), stone, tax.
 *
 * Provisional defaults until business signs CALCULATION-RULES.md:
 * - net = gross - stone (if net not supplied)
 * - fine = net * purity%
 * - wastage ADD_TO_PURITY => finalFine = net * (purity + wastage)%
 * - metal = finalFine * rate
 * - making PER_GRAM on net weight by default
 */
@Component
public class MetalValuationEngine {

    public static final String MODE_ADD_TO_NET_PCT = "ADD_TO_NET_PCT";
    public static final String MODE_ADD_TO_PURITY = "ADD_TO_PURITY";

    public MetalLineResult calculate(MetalLineInput input) {
        BigDecimal qty = nz(input.quantity(), BigDecimal.ONE);
        BigDecimal gross = MoneyMath.weight(nz(input.grossWeight()));
        BigDecimal stoneWt = MoneyMath.weight(nz(input.stoneWeight()));
        BigDecimal net = input.netWeight() != null
                ? MoneyMath.weight(input.netWeight())
                : MoneyMath.weight(gross.subtract(stoneWt));
        if (net.compareTo(BigDecimal.ZERO) < 0) {
            net = BigDecimal.ZERO.setScale(MoneyMath.WEIGHT_SCALE, MoneyMath.ROUNDING);
        }

        BigDecimal purity = MoneyMath.purity(nz(input.purityPercent()));
        BigDecimal wastage = MoneyMath.purity(nz(input.wastagePercent()));
        BigDecimal fine = MoneyMath.fineWeight(net, purity);
        BigDecimal custWstg = MoneyMath.purity(nz(input.custWastagePercent()));
        String custMode = input.sellCustWstgMode() == null || input.sellCustWstgMode().isBlank()
                ? MODE_ADD_TO_NET_PCT
                : input.sellCustWstgMode().trim().toUpperCase();
        BigDecimal finalFine;
        if (custWstg.signum() == 0) {
            finalFine = MoneyMath.fineWeight(net, MoneyMath.purity(purity.add(wastage)));
        } else if (MODE_ADD_TO_PURITY.equals(custMode)) {
            finalFine = MoneyMath.fineWeight(net, MoneyMath.purity(purity.add(wastage).add(custWstg)));
        } else {
            // Reference sell: FFINE WT = NT × (100 + custWstg)% ; metal = FFINE × rate
            // (bill rate is treated as already purity-aware for that line)
            finalFine = MoneyMath.weight(net.multiply(BigDecimal.valueOf(100).add(custWstg))
                    .divide(BigDecimal.valueOf(100), 8, RoundingMode.HALF_UP));
        }

        BigDecimal rate = MoneyMath.rate(nz(input.ratePerGram()));
        BigDecimal metalAmount = MoneyMath.money(finalFine.multiply(rate));

        String makingType = input.makingType() == null || input.makingType().isBlank()
                ? "PER_GRAM"
                : input.makingType().toUpperCase();
        BigDecimal makingRate = MoneyMath.rate(nz(input.makingRate()));
        BigDecimal makingAmount = switch (makingType) {
            case "PERCENT", "PER" -> MoneyMath.money(metalAmount.multiply(makingRate)
                    .divide(BigDecimal.valueOf(100), 8, RoundingMode.HALF_UP));
            case "FIXED", "FX" -> MoneyMath.money(makingRate);
            case "PP", "PER_PIECE" -> MoneyMath.money(makingRate.multiply(qty));
            case "KG" -> MoneyMath.money(makingRate.multiply(
                    net.divide(BigDecimal.valueOf(1000), 8, RoundingMode.HALF_UP)).multiply(qty));
            default -> MoneyMath.money(net.multiply(makingRate).multiply(qty)); // GM / PER_GRAM
        };

        BigDecimal stoneAmt = MoneyMath.money(nz(input.stoneAmount()));
        BigDecimal discount = MoneyMath.money(nz(input.discountAmount()));
        BigDecimal valueAdded = MoneyMath.money(nz(input.valueAdded()));
        BigDecimal taxable = MoneyMath.money(metalAmount.add(makingAmount).add(stoneAmt).add(valueAdded).subtract(discount));
        if (taxable.compareTo(BigDecimal.ZERO) < 0) {
            taxable = BigDecimal.ZERO.setScale(MoneyMath.MONEY_SCALE, MoneyMath.ROUNDING);
        }

        BigDecimal cgstPct = nz(input.cgstPercent());
        BigDecimal sgstPct = nz(input.sgstPercent());
        BigDecimal igstPct = nz(input.igstPercent());
        BigDecimal cgst = MoneyMath.money(taxable.multiply(cgstPct).divide(BigDecimal.valueOf(100), 8, RoundingMode.HALF_UP));
        BigDecimal sgst = MoneyMath.money(taxable.multiply(sgstPct).divide(BigDecimal.valueOf(100), 8, RoundingMode.HALF_UP));
        BigDecimal igst = MoneyMath.money(taxable.multiply(igstPct).divide(BigDecimal.valueOf(100), 8, RoundingMode.HALF_UP));
        BigDecimal tax = MoneyMath.money(cgst.add(sgst).add(igst));
        BigDecimal lineTotal = MoneyMath.money(taxable.add(tax));

        String notes = "net=gross-stone; fine=net*purity%; finalFine=net*(purity+wastage)%; metal=finalFine*rate; making="
                + makingType + (custWstg.signum() == 0 ? "" : "; custWstg=" + custWstg + "% " + custMode);

        return new MetalLineResult(
                gross, stoneWt, net, purity, wastage, fine, finalFine, rate,
                metalAmount, makingType, makingRate, makingAmount, stoneAmt, discount,
                taxable, cgst, sgst, igst, tax, lineTotal, notes, custWstg, valueAdded
        );
    }

    private BigDecimal nz(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private BigDecimal nz(BigDecimal value, BigDecimal fallback) {
        return value == null ? fallback : value;
    }
}
