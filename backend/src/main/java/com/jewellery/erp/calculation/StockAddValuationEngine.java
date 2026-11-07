package com.jewellery.erp.calculation;

import com.jewellery.erp.shared.domain.MoneyMath;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;

/**
 * Settings-driven Add Stock valuation (Omunim calStockItemPrice / changeNetWeightByPktWt behaviour).
 *
 * <pre>
 * net = gross − (pkt + less + crystalAutoLess)
 * fine = fineBaseWt × purity%
 * finalFine = byDefault → fineBaseWt × (purity+wastage)%
 *           | else → wastageBase×wastage% + (fine|net|gross)
 * metal = valWt × rate
 * final = metal + lab + other + hallmark + stone + tax
 * </pre>
 * Making is computed and stored for sell; not added into Add Stock metal final (reference).
 */
@Component
public class StockAddValuationEngine {

    public StockAddResult calculate(StockAddInput input) {
        StockAddSettings settings = input.settings() == null ? StockAddSettings.defaults() : input.settings();
        BigDecimal qty = nz(input.quantity(), BigDecimal.ONE);
        BigDecimal gross = MoneyMath.weight(nz(input.grossWeight()));
        BigDecimal pkt = MoneyMath.weight(nz(input.pktWeight()));
        BigDecimal less = MoneyMath.weight(nz(input.lessWeight()));

        List<StockAddInput.CrystalLine> crystals = input.crystals() == null ? List.of() : input.crystals();
        BigDecimal crystalLess = BigDecimal.ZERO.setScale(MoneyMath.WEIGHT_SCALE, MoneyMath.ROUNDING);
        BigDecimal stoneVal = BigDecimal.ZERO.setScale(MoneyMath.MONEY_SCALE, MoneyMath.ROUNDING);
        for (StockAddInput.CrystalLine c : crystals) {
            BigDecimal cQty = nz(c.quantity(), BigDecimal.ONE);
            BigDecimal cWt = MoneyMath.weight(nz(c.weight()));
            if (c.autoLess()) {
                crystalLess = MoneyMath.weight(crystalLess.add(cWt));
            }
            stoneVal = MoneyMath.money(stoneVal.add(chargeAmount(nz(c.rate()), c.rateType(), cQty, cWt, BigDecimal.ZERO)));
        }

        BigDecimal net = MoneyMath.weight(gross.subtract(pkt).subtract(less).subtract(crystalLess));
        if (net.compareTo(BigDecimal.ZERO) < 0) {
            net = BigDecimal.ZERO.setScale(MoneyMath.WEIGHT_SCALE, MoneyMath.ROUNDING);
        }

        BigDecimal purity = MoneyMath.purity(nz(input.purityPercent()));
        BigDecimal wastage = MoneyMath.purity(nz(input.wastagePercent()));
        BigDecimal finalPurity = MoneyMath.purity(purity.add(wastage));

        BigDecimal fineBaseWt = resolveWeight(settings.stockFineBaseBy(), gross, net, null, null);
        BigDecimal fine = MoneyMath.fineWeight(fineBaseWt, purity);

        BigDecimal wastageBase = resolveWastageBase(settings.stockCsWstgBy(), gross, net, fine);
        BigDecimal wastageWt = MoneyMath.weight(
                wastageBase.multiply(wastage).divide(BigDecimal.valueOf(100), 8, RoundingMode.HALF_UP));

        String finalFineBy = normalize(settings.addStockFinalFinewt());
        BigDecimal finalFine;
        if ("byDefault".equalsIgnoreCase(finalFineBy) || finalFineBy.isBlank()) {
            finalFine = MoneyMath.fineWeight(fineBaseWt, finalPurity);
        } else if ("byFineWt".equalsIgnoreCase(finalFineBy)) {
            finalFine = MoneyMath.weight(fine.add(wastageWt));
        } else if ("byGrossWt".equalsIgnoreCase(finalFineBy)) {
            finalFine = MoneyMath.weight(gross.add(wastageWt));
        } else {
            // byNetWt
            finalFine = MoneyMath.weight(net.add(wastageWt));
        }

        BigDecimal rate = MoneyMath.rate(nz(input.metalRatePerGram()));
        BigDecimal valWt = resolveWeight(settings.stockValBy(), gross, net, fine, finalFine);
        BigDecimal metalVal = MoneyMath.money(valWt.multiply(rate));

        BigDecimal labBasis = resolveWeight(settings.stockLbrBy(), gross, net, fine, finalFine);
        BigDecimal labTotal = MoneyMath.money(chargeAmount(
                nz(input.labCharges()), input.labChargesType(), qty, labBasis, metalVal));

        BigDecimal mkgBasis = resolveWeight(settings.stockMkgBy(), gross, net, fine, finalFine);
        BigDecimal mkgTotal = MoneyMath.money(chargeAmount(
                nz(input.makingCharges()), input.makingChargesType(), qty, mkgBasis, metalVal));

        BigDecimal otherTotal = MoneyMath.money(chargeAmount(
                nz(input.otherCharges()), input.otherChargesType(), qty, net, metalVal));

        BigDecimal hallmarkTotal = MoneyMath.money(chargeAmount(
                nz(input.hallmarkCharges()), input.hallmarkChargesType(), qty, net, metalVal));

        BigDecimal taxable = MoneyMath.money(metalVal.add(labTotal));
        BigDecimal taxPct = nz(input.taxPercent());
        BigDecimal taxAmt = MoneyMath.money(
                taxable.multiply(taxPct).divide(BigDecimal.valueOf(100), 8, RoundingMode.HALF_UP));

        // Making stored for sell; Add Stock final follows reference (metal+lab+other+tax+hallmark+stone)
        BigDecimal finalVal = MoneyMath.money(
                metalVal.add(labTotal).add(otherTotal).add(hallmarkTotal).add(stoneVal).add(taxAmt));

        String notes = "net=gs-(pkt+less+cryLess); fine=fineBase*purity%; finalFine=" + finalFineBy
                + "; metal=valWt(" + settings.stockValBy() + ")*rate; +lab+other+hallmark+stone+tax; making stored only";

        return new StockAddResult(
                gross, pkt, less, crystalLess, net,
                purity, wastage, finalPurity, fine, wastageWt, finalFine,
                rate, metalVal, labTotal, mkgTotal, otherTotal, hallmarkTotal, stoneVal, taxAmt, finalVal,
                settings.stockValBy(), settings.stockMkgBy(), settings.stockLbrBy(),
                settings.stockCsWstgBy(), settings.addStockFinalFinewt(), settings.stockFineBaseBy(),
                notes
        );
    }

    private BigDecimal resolveWastageBase(String key, BigDecimal gross, BigDecimal net, BigDecimal fine) {
        String k = normalize(key);
        if (k.contains("gross")) {
            return gross;
        }
        if (k.contains("net")) {
            return net;
        }
        return fine;
    }

    private BigDecimal resolveWeight(String key, BigDecimal gross, BigDecimal net, BigDecimal fine, BigDecimal finalFine) {
        String k = normalize(key);
        if (k.contains("ffine") || k.contains("finalfine") || k.contains("byffine")) {
            return finalFine != null ? finalFine : fine;
        }
        if (k.contains("fine")) {
            return fine != null ? fine : net;
        }
        if (k.contains("gross")) {
            return gross;
        }
        return net;
    }

    /**
     * Charge types: GM, KG, MG, PP (per piece), FX (fixed), PER (percent of metal).
     */
    public BigDecimal chargeAmount(BigDecimal rate, String type, BigDecimal qty, BigDecimal weightGrams, BigDecimal metalAmount) {
        String t = type == null || type.isBlank() ? "GM" : type.trim().toUpperCase(Locale.ROOT);
        return switch (t) {
            case "KG" -> rate.multiply(weightGrams.divide(BigDecimal.valueOf(1000), 8, RoundingMode.HALF_UP));
            case "MG" -> rate.multiply(weightGrams.multiply(BigDecimal.valueOf(1000)));
            case "PP", "PER_PIECE" -> rate.multiply(qty);
            case "FX", "FIXED" -> rate;
            case "PER", "PERCENT", "%" -> metalAmount.multiply(rate).divide(BigDecimal.valueOf(100), 8, RoundingMode.HALF_UP);
            default -> rate.multiply(weightGrams); // GM
        };
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT).replace("_", "");
    }

    private BigDecimal nz(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private BigDecimal nz(BigDecimal value, BigDecimal fallback) {
        return value == null ? fallback : value;
    }
}
