package com.jewellery.erp.credit.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public final class CreditDtos {

    private CreditDtos() {
    }

    public record PostEntryRequest(
            @NotBlank String branchId,
            @NotBlank String customerId,
            @NotNull @DecimalMin(value = "0.01", message = "amount must be greater than zero") BigDecimal amount,
            LocalDate entryDate,
            @Size(max = 32) String refType,
            @Size(max = 36) String refId,
            @Size(max = 500) String remarks
    ) {
    }

    public record LedgerEntryResponse(
            String id,
            String branchId,
            String customerId,
            String entryType,
            String refType,
            String refId,
            String refNo,
            LocalDate entryDate,
            BigDecimal amount,
            BigDecimal balanceAfter,
            String remarks,
            Instant createdAt
    ) {
    }

    public record CustomerBalanceResponse(
            String customerId,
            String customerCode,
            String customerName,
            String phone,
            BigDecimal balance
    ) {
    }

    public record CustomerLedgerResponse(
            String customerId,
            BigDecimal udhaarOutstanding,
            BigDecimal advanceBalance,
            java.util.List<LedgerEntryResponse> entries
    ) {
    }
}
