package com.jewellery.erp.loans.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class LoanDtos {

    private LoanDtos() {
    }

    public record CreateLoanRequest(
            @NotBlank String branchId,
            @NotBlank String customerId,
            LocalDate openDate,
            @NotNull @DecimalMin(value = "0.01", message = "principalAmount must be greater than zero") BigDecimal principalAmount,
            @DecimalMin("0") BigDecimal roiPercent,
            @Size(max = 32) String interestType,
            @Size(max = 32) String metalType,
            @DecimalMin("0") BigDecimal grossWeight,
            @DecimalMin("0") BigDecimal netWeight,
            @DecimalMin("0") BigDecimal fineWeight,
            @DecimalMin("0") BigDecimal valuationAmount,
            @Size(max = 500) String itemDesc,
            @Size(max = 500) String remarks
    ) {
    }

    public record LoanResponse(
            String id,
            String branchId,
            String customerId,
            String ticketNo,
            LocalDate openDate,
            String status,
            BigDecimal principalAmount,
            BigDecimal roiPercent,
            String interestType,
            String metalType,
            BigDecimal grossWeight,
            BigDecimal netWeight,
            BigDecimal fineWeight,
            BigDecimal valuationAmount,
            long daysElapsed,
            BigDecimal interestAmount,
            BigDecimal totalAmount,
            String itemDesc,
            String remarks
    ) {
    }
}
