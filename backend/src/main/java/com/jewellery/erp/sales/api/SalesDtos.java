package com.jewellery.erp.sales.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public final class SalesDtos {

    private SalesDtos() {
    }

    public record LineRequest(
            String stockItemId,
            @NotBlank String itemName,
            String metalId,
            String purityId,
            BigDecimal quantity,
            @NotNull BigDecimal grossWeight,
            BigDecimal stoneWeight,
            BigDecimal netWeight,
            @NotNull BigDecimal purityPercent,
            BigDecimal wastagePercent,
            @NotNull BigDecimal ratePerGram,
            String makingType,
            BigDecimal makingRate,
            BigDecimal stoneAmount,
            BigDecimal discountAmount,
            BigDecimal cgstPercent,
            BigDecimal sgstPercent,
            BigDecimal igstPercent,
            @Size(max = 64) String barcode,
            @DecimalMin("0") BigDecimal custWastagePercent,
            @DecimalMin("0") BigDecimal valueAdded
    ) {
    }

    public record ExchangeLineRequest(
            @Size(max = 32) String metalType,
            @Size(max = 255) String description,
            @NotNull @DecimalMin("0") BigDecimal grossWeight,
            @DecimalMin("0") BigDecimal lessWeight,
            BigDecimal netWeight,
            @NotNull @DecimalMin("0") BigDecimal purityPercent,
            @NotNull @DecimalMin("0") BigDecimal ratePerGram
    ) {
    }

    public record CreateSaleRequest(
            @NotBlank String branchId,
            String customerId,
            LocalDate invoiceDate,
            BigDecimal paidAmount,
            String remarks,
            @NotEmpty @Valid List<LineRequest> lines,
            @DecimalMin("0") BigDecimal cashAmount,
            @DecimalMin("0") BigDecimal cardAmount,
            @DecimalMin("0") BigDecimal onlineAmount,
            @DecimalMin("0") BigDecimal chequeAmount,
            @DecimalMin("0") BigDecimal udhaarAmount,
            @DecimalMin("0") BigDecimal hallmarkAmount,
            @DecimalMin("0") BigDecimal otherCharges,
            @DecimalMin("0") BigDecimal courierCharges,
            @Valid List<ExchangeLineRequest> exchangeLines
    ) {
    }

    public record LineResponse(
            String id,
            int lineNo,
            String stockItemId,
            String itemName,
            BigDecimal quantity,
            BigDecimal grossWeight,
            BigDecimal netWeight,
            BigDecimal purityPercent,
            BigDecimal wastagePercent,
            BigDecimal fineWeight,
            BigDecimal finalFineWeight,
            BigDecimal ratePerGram,
            BigDecimal metalAmount,
            String makingType,
            BigDecimal makingAmount,
            BigDecimal stoneAmount,
            BigDecimal lineTotal,
            String barcode,
            BigDecimal custWastagePercent,
            BigDecimal valueAdded
    ) {
    }

    public record ExchangeLineResponse(
            String id,
            int lineNo,
            String metalType,
            String description,
            BigDecimal grossWeight,
            BigDecimal lessWeight,
            BigDecimal netWeight,
            BigDecimal purityPercent,
            BigDecimal fineWeight,
            BigDecimal ratePerGram,
            BigDecimal valuation
    ) {
    }

    public record SaleResponse(
            String id,
            String invoiceNo,
            LocalDate invoiceDate,
            String status,
            String branchId,
            String customerId,
            BigDecimal metalAmount,
            BigDecimal makingAmount,
            BigDecimal stoneAmount,
            BigDecimal discountAmount,
            BigDecimal taxableAmount,
            BigDecimal cgstAmount,
            BigDecimal sgstAmount,
            BigDecimal igstAmount,
            BigDecimal taxAmount,
            BigDecimal grandTotal,
            BigDecimal paidAmount,
            List<LineResponse> lines,
            BigDecimal cashAmount,
            BigDecimal cardAmount,
            BigDecimal onlineAmount,
            BigDecimal chequeAmount,
            BigDecimal udhaarAmount,
            BigDecimal exchangeAmount,
            BigDecimal hallmarkAmount,
            BigDecimal otherCharges,
            BigDecimal courierCharges,
            List<ExchangeLineResponse> exchangeLines
    ) {
    }
}
