package com.jewellery.erp.shared.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Canonical decimal helpers. Never use floating point for money, rates, or weights.
 */
public final class MoneyMath {

    public static final int MONEY_SCALE = 2;
    public static final int RATE_SCALE = 4;
    public static final int WEIGHT_SCALE = 4;
    public static final int PURITY_SCALE = 4;
    public static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    private MoneyMath() {
    }

    public static BigDecimal money(BigDecimal value) {
        return require(value).setScale(MONEY_SCALE, ROUNDING);
    }

    public static BigDecimal rate(BigDecimal value) {
        return require(value).setScale(RATE_SCALE, ROUNDING);
    }

    public static BigDecimal weight(BigDecimal value) {
        return require(value).setScale(WEIGHT_SCALE, ROUNDING);
    }

    public static BigDecimal purity(BigDecimal value) {
        return require(value).setScale(PURITY_SCALE, ROUNDING);
    }

    public static BigDecimal fineWeight(BigDecimal netWeight, BigDecimal finenessPercent) {
        // finenessPercent e.g. 91.6000 means 91.6%
        BigDecimal factor = purity(finenessPercent).divide(BigDecimal.valueOf(100), 8, ROUNDING);
        return weight(weight(netWeight).multiply(factor));
    }

    private static BigDecimal require(BigDecimal value) {
        if (value == null) {
            throw new BusinessException("NULL_DECIMAL", "Decimal value is required");
        }
        return value;
    }
}
