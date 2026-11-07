package com.jewellery.erp.calculation;

import java.math.BigDecimal;

public record StockAddResult(
        BigDecimal grossWeight,
        BigDecimal pktWeight,
        BigDecimal lessWeight,
        BigDecimal crystalLessWeight,
        BigDecimal netWeight,
        BigDecimal purityPercent,
        BigDecimal wastagePercent,
        BigDecimal finalPurityPercent,
        BigDecimal fineWeight,
        BigDecimal wastageWeight,
        BigDecimal finalFineWeight,
        BigDecimal metalRatePerGram,
        BigDecimal metalValuation,
        BigDecimal totalLabCharges,
        BigDecimal totalMakingCharges,
        BigDecimal totalOtherCharges,
        BigDecimal totalHallmarkCharges,
        BigDecimal stoneValuation,
        BigDecimal taxAmount,
        BigDecimal finalValuation,
        String valuationBy,
        String makingBy,
        String labBy,
        String wastageBy,
        String finalFineBy,
        String fineBaseBy,
        String formulaNotes
) {
}
