package com.jewellery.erp.shared.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MoneyMathTest {

    @Test
    void fineWeightUsesPurityPercent() {
        BigDecimal fine = MoneyMath.fineWeight(new BigDecimal("10.0000"), new BigDecimal("91.6000"));
        assertEquals(new BigDecimal("9.1600"), fine);
    }

    @Test
    void moneyRoundsHalfUp() {
        assertEquals(new BigDecimal("10.13"), MoneyMath.money(new BigDecimal("10.125")));
    }
}
