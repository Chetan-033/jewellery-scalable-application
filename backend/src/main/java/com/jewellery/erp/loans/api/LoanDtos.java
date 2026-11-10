package com.jewellery.erp.loans.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public final class LoanDtos {

    private LoanDtos() {
    }

    public record PledgedItemDto(
            String id,
            String metalType,
            @NotBlank String itemName,
            Integer quantity,
            BigDecimal grossWeight,
            BigDecimal netWeight,
            BigDecimal lessWeight,
            BigDecimal purityPercent,
            BigDecimal fineWeight,
            BigDecimal valuationAmount,
            String photoUrl
    ) {
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
            @Size(max = 500) String remarks,
            // Extended fields
            String packetNo,
            String boxNo,
            String loanType,
            Boolean firstMonthInterest,
            Integer timePeriodMonths,
            String drAccount,
            String itemLocation,
            BigDecimal cashAmount,
            String cashNarration,
            BigDecimal bankAmount,
            String bankNarration,
            BigDecimal cardAmount,
            String cardNarration,
            BigDecimal onlineAmount,
            String onlineNarration,
            List<PledgedItemDto> items
    ) {
    }

    public record LoanResponse(
            String id,
            String branchId,
            String customerId,
            String customerName,
            String customerCode,
            String customerPhone,
            String customerCity,
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
            String remarks,
            // Extended fields
            String packetNo,
            String boxNo,
            String loanType,
            Boolean firstMonthInterest,
            Integer timePeriodMonths,
            String drAccount,
            String itemLocation,
            BigDecimal cashAmount,
            String cashNarration,
            BigDecimal bankAmount,
            String bankNarration,
            BigDecimal cardAmount,
            String cardNarration,
            BigDecimal onlineAmount,
            String onlineNarration,
            List<PledgedItemDto> items,
            // Release fields
            LocalDate releaseDate,
            String releaseTicketNo,
            BigDecimal discountAmount,
            BigDecimal extraAmount,
            BigDecimal paidAmount,
            BigDecimal paidInterest,
            String monthlyInterestOption,
            BigDecimal releaseCashAmount,
            BigDecimal releaseBankAmount,
            BigDecimal releaseCardAmount,
            BigDecimal releaseOnlineAmount,
            String releaseRemarks,
            String lockerStatus
    ) {
    }

    public record ReleaseLoanRequest(
            LocalDate releaseDate,
            String monthlyInterestOption, // DD, FM, HM, 7D
            BigDecimal customInterestAmount,
            BigDecimal discountAmount,
            BigDecimal extraAmount,
            BigDecimal cashAmount,
            String cashNarration,
            BigDecimal bankAmount,
            String bankNarration,
            BigDecimal cardAmount,
            String cardNarration,
            BigDecimal onlineAmount,
            String onlineNarration,
            String remarks
    ) {
    }

    public record SettlementPreviewResponse(
            String loanId,
            String ticketNo,
            LocalDate openDate,
            LocalDate releaseDate,
            long daysElapsed,
            long monthsCharged,
            long daysCharged,
            String monthlyInterestOption,
            BigDecimal principalAmount,
            BigDecimal roiPercent,
            String interestType,
            BigDecimal accruedInterest,
            BigDecimal discountAmount,
            BigDecimal extraAmount,
            BigDecimal totalAmountDue
    ) {
    }
}
