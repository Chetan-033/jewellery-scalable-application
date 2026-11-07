package com.jewellery.erp.calculation;

import java.math.BigDecimal;

public record MetalLineInput(
        BigDecimal quantity,
        BigDecimal grossWeight,
        BigDecimal stoneWeight,
        BigDecimal netWeight,
        BigDecimal purityPercent,
        BigDecimal wastagePercent,
        BigDecimal ratePerGram,
        String makingType,
        BigDecimal makingRate,
        BigDecimal stoneAmount,
        BigDecimal discountAmount,
        BigDecimal cgstPercent,
        BigDecimal sgstPercent,
        BigDecimal igstPercent,
        BigDecimal custWastagePercent,
        String sellCustWstgMode,
        BigDecimal valueAdded
) {

    /** Backward-compatible constructor (no customer wastage / value added). */
    public MetalLineInput(
            BigDecimal quantity,
            BigDecimal grossWeight,
            BigDecimal stoneWeight,
            BigDecimal netWeight,
            BigDecimal purityPercent,
            BigDecimal wastagePercent,
            BigDecimal ratePerGram,
            String makingType,
            BigDecimal makingRate,
            BigDecimal stoneAmount,
            BigDecimal discountAmount,
            BigDecimal cgstPercent,
            BigDecimal sgstPercent,
            BigDecimal igstPercent) {
        this(quantity, grossWeight, stoneWeight, netWeight, purityPercent, wastagePercent, ratePerGram,
                makingType, makingRate, stoneAmount, discountAmount, cgstPercent, sgstPercent, igstPercent,
                null, null, null);
    }
}
