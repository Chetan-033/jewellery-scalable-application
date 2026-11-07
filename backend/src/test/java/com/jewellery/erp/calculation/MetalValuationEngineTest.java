package com.jewellery.erp.calculation;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MetalValuationEngineTest {

    private final MetalValuationEngine engine = new MetalValuationEngine();

    @Test
    void calculatesFineMetalMakingAndTaxLikeReferenceFields() {
        MetalLineResult result = engine.calculate(new MetalLineInput(
                BigDecimal.ONE,
                new BigDecimal("10.0000"),
                BigDecimal.ZERO,
                null,
                new BigDecimal("91.6000"),
                new BigDecimal("1.0000"),
                new BigDecimal("6000.0000"),
                "PER_GRAM",
                new BigDecimal("100.0000"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                new BigDecimal("1.5"),
                new BigDecimal("1.5"),
                BigDecimal.ZERO
        ));

        assertEquals(new BigDecimal("9.1600"), result.fineWeight());
        assertEquals(new BigDecimal("9.2600"), result.finalFineWeight());
        assertEquals(new BigDecimal("55560.00"), result.metalAmount());
        assertEquals(new BigDecimal("1000.00"), result.makingAmount());
        assertEquals(new BigDecimal("56560.00"), result.taxableAmount());
        assertEquals(new BigDecimal("848.40"), result.cgstAmount());
        assertEquals(new BigDecimal("848.40"), result.sgstAmount());
        assertEquals(new BigDecimal("58256.80"), result.lineTotal());
    }

    @Test
    void makingPpIsPerPieceAndCustWastageGrossesNet() {
        MetalLineResult result = engine.calculate(new MetalLineInput(
                new BigDecimal("2"),
                new BigDecimal("2.6000"),
                BigDecimal.ZERO,
                null,
                new BigDecimal("92.0000"),
                BigDecimal.ZERO,
                new BigDecimal("14250.0000"),
                "PP",
                new BigDecimal("1200.0000"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                new BigDecimal("18.0000"),
                MetalValuationEngine.MODE_ADD_TO_NET_PCT,
                new BigDecimal("6669.00")
        ));

        // net 2.6 * 1.18 = 3.068; fine/finalFine with 92%
        assertEquals(new BigDecimal("3.0680"), result.finalFineWeight());
        assertEquals(new BigDecimal("2400.00"), result.makingAmount());
        assertEquals(new BigDecimal("6669.00"), result.valueAdded());
    }
}
