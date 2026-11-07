package com.jewellery.erp.calculation;

import java.math.BigDecimal;

public record MetalLineResult(
        BigDecimal grossWeight,
        BigDecimal stoneWeight,
        BigDecimal netWeight,
        BigDecimal purityPercent,
        BigDecimal wastagePercent,
        BigDecimal fineWeight,
        BigDecimal finalFineWeight,
        BigDecimal ratePerGram,
        BigDecimal metalAmount,
        String makingType,
        BigDecimal makingRate,
        BigDecimal makingAmount,
        BigDecimal stoneAmount,
        BigDecimal discountAmount,
        BigDecimal taxableAmount,
        BigDecimal cgstAmount,
        BigDecimal sgstAmount,
        BigDecimal igstAmount,
        BigDecimal taxAmount,
        BigDecimal lineTotal,
        String formulaNotes,
        BigDecimal custWastagePercent,
        BigDecimal valueAdded
) {
}
