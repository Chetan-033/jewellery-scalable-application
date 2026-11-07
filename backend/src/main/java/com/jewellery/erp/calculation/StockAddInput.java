package com.jewellery.erp.calculation;

import java.math.BigDecimal;
import java.util.List;

public record StockAddInput(
        BigDecimal quantity,
        BigDecimal grossWeight,
        BigDecimal pktWeight,
        BigDecimal lessWeight,
        BigDecimal purityPercent,
        BigDecimal wastagePercent,
        BigDecimal metalRatePerGram,
        BigDecimal labCharges,
        String labChargesType,
        BigDecimal makingCharges,
        String makingChargesType,
        BigDecimal otherCharges,
        String otherChargesType,
        BigDecimal hallmarkCharges,
        String hallmarkChargesType,
        BigDecimal taxPercent,
        List<CrystalLine> crystals,
        StockAddSettings settings
) {
    public record CrystalLine(
            BigDecimal quantity,
            BigDecimal weight,
            BigDecimal rate,
            String rateType,
            boolean autoLess
    ) {
    }
}
