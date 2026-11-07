package com.jewellery.erp.calculation;

/**
 * Add-stock calculation settings (omlayout equivalents).
 */
public record StockAddSettings(
        String stockValBy,
        String stockMkgBy,
        String stockCsWstgBy,
        String stockLbrBy,
        String addStockFinalFinewt,
        String stockFineBaseBy
) {
    public static StockAddSettings defaults() {
        return new StockAddSettings(
                "byFFineWt",
                "mkgByNetWt",
                "custWastgByFineWt",
                "lbByNetWt",
                "byDefault",
                "byNetWt"
        );
    }
}
