package com.jewellery.erp.calculation;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StockAddValuationEngineTest {

    private final StockAddValuationEngine engine = new StockAddValuationEngine();

    @Test
    void netsWeightsAndValuesByFinalFineWithLab() {
        StockAddSettings settings = new StockAddSettings(
                "byFFineWt",
                "mkgByNetWt",
                "custWastgByFineWt",
                "lbByNetWt",
                "byDefault",
                "byNetWt"
        );

        StockAddResult result = engine.calculate(new StockAddInput(
                BigDecimal.ONE,
                new BigDecimal("10.0000"),
                new BigDecimal("0.5000"),
                new BigDecimal("0.5000"),
                new BigDecimal("91.6000"),
                new BigDecimal("1.0000"),
                new BigDecimal("6000.0000"),
                new BigDecimal("50.0000"),
                "GM",
                new BigDecimal("100.0000"),
                "GM",
                BigDecimal.ZERO,
                "FX",
                BigDecimal.ZERO,
                "FX",
                BigDecimal.ZERO,
                List.of(),
                settings
        ));

        // net = 10 - 0.5 - 0.5 = 9
        assertEquals(new BigDecimal("9.0000"), result.netWeight());
        // fine = 9 * 91.6% = 8.2440
        assertEquals(new BigDecimal("8.2440"), result.fineWeight());
        // finalFine = 9 * 92.6% = 8.3340
        assertEquals(new BigDecimal("8.3340"), result.finalFineWeight());
        // metal = 8.3340 * 6000
        assertEquals(new BigDecimal("50004.00"), result.metalValuation());
        // lab = 50 * net(9) = 450
        assertEquals(new BigDecimal("450.00"), result.totalLabCharges());
        // making computed but not in final
        assertEquals(new BigDecimal("900.00"), result.totalMakingCharges());
        assertEquals(new BigDecimal("50454.00"), result.finalValuation());
    }

    @Test
    void crystalAutoLessReducesNet() {
        StockAddSettings settings = StockAddSettings.defaults();
        StockAddResult result = engine.calculate(new StockAddInput(
                BigDecimal.ONE,
                new BigDecimal("10.0000"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                new BigDecimal("100.0000"),
                BigDecimal.ZERO,
                new BigDecimal("1000.0000"),
                BigDecimal.ZERO,
                "GM",
                BigDecimal.ZERO,
                "GM",
                BigDecimal.ZERO,
                "FX",
                BigDecimal.ZERO,
                "FX",
                BigDecimal.ZERO,
                List.of(new StockAddInput.CrystalLine(
                        BigDecimal.ONE, new BigDecimal("1.0000"), new BigDecimal("500"), "PP", true)),
                settings
        ));

        assertEquals(new BigDecimal("9.0000"), result.netWeight());
        assertEquals(new BigDecimal("500.00"), result.stoneValuation());
    }
}
