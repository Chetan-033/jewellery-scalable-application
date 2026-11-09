package com.jewellery.erp.loans.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "girvi_loan")
public class GirviLoan {

    public static final String ACTIVE = "ACTIVE";
    public static final String RELEASED = "RELEASED";

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "branch_id", nullable = false, length = 36)
    private String branchId;

    @Column(name = "customer_id", nullable = false, length = 36)
    private String customerId;

    @Column(name = "ticket_no", nullable = false, unique = true, length = 64)
    private String ticketNo;

    @Column(name = "open_date", nullable = false)
    private LocalDate openDate;

    @Column(nullable = false, length = 32)
    private String status = ACTIVE;

    @Column(name = "principal_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal principalAmount = BigDecimal.ZERO;

    @Column(name = "roi_percent", nullable = false, precision = 10, scale = 4)
    private BigDecimal roiPercent = BigDecimal.ZERO;

    @Column(name = "interest_type", nullable = false, length = 32)
    private String interestType = "MONTHLY";

    @Column(name = "metal_type", length = 32)
    private String metalType;

    @Column(name = "gross_weight", nullable = false, precision = 18, scale = 4)
    private BigDecimal grossWeight = BigDecimal.ZERO;

    @Column(name = "net_weight", nullable = false, precision = 18, scale = 4)
    private BigDecimal netWeight = BigDecimal.ZERO;

    @Column(name = "fine_weight", nullable = false, precision = 18, scale = 4)
    private BigDecimal fineWeight = BigDecimal.ZERO;

    @Column(name = "valuation_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal valuationAmount = BigDecimal.ZERO;

    @Column(name = "interest_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal interestAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "item_desc", length = 500)
    private String itemDesc;

    @Column(length = 500)
    private String remarks;

    // Extended Girvi Details
    @Column(name = "packet_no", length = 64)
    private String packetNo;

    @Column(name = "box_no", length = 64)
    private String boxNo;

    @Column(name = "loan_type", length = 32)
    private String loanType = "SECURED";

    @Column(name = "first_month_interest", nullable = false)
    private Boolean firstMonthInterest = false;

    @Column(name = "time_period_months", nullable = false)
    private Integer timePeriodMonths = 12;

    @Column(name = "dr_account", length = 100)
    private String drAccount = "Secured Loans";

    @Column(name = "item_location", length = 100)
    private String itemLocation;

    @Column(name = "cash_amount", precision = 18, scale = 2)
    private BigDecimal cashAmount = BigDecimal.ZERO;

    @Column(name = "cash_narration", length = 255)
    private String cashNarration;

    @Column(name = "bank_amount", precision = 18, scale = 2)
    private BigDecimal bankAmount = BigDecimal.ZERO;

    @Column(name = "bank_narration", length = 255)
    private String bankNarration;

    @Column(name = "card_amount", precision = 18, scale = 2)
    private BigDecimal cardAmount = BigDecimal.ZERO;

    @Column(name = "card_narration", length = 255)
    private String cardNarration;

    @Column(name = "online_amount", precision = 18, scale = 2)
    private BigDecimal onlineAmount = BigDecimal.ZERO;

    @Column(name = "online_narration", length = 255)
    private String onlineNarration;

    @Column(name = "items_json", columnDefinition = "TEXT")
    private String itemsJson;

    // Release Settlement Details (Omunim Core Logic)
    @Column(name = "release_date")
    private LocalDate releaseDate;

    @Column(name = "release_ticket_no", length = 64)
    private String releaseTicketNo;

    @Column(name = "discount_amount", precision = 18, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "extra_amount", precision = 18, scale = 2)
    private BigDecimal extraAmount = BigDecimal.ZERO;

    @Column(name = "paid_amount", precision = 18, scale = 2)
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Column(name = "paid_interest", precision = 18, scale = 2)
    private BigDecimal paidInterest = BigDecimal.ZERO;

    @Column(name = "monthly_interest_option", length = 16)
    private String monthlyInterestOption = "DD";

    @Column(name = "release_cash_amount", precision = 18, scale = 2)
    private BigDecimal releaseCashAmount = BigDecimal.ZERO;

    @Column(name = "release_bank_amount", precision = 18, scale = 2)
    private BigDecimal releaseBankAmount = BigDecimal.ZERO;

    @Column(name = "release_card_amount", precision = 18, scale = 2)
    private BigDecimal releaseCardAmount = BigDecimal.ZERO;

    @Column(name = "release_online_amount", precision = 18, scale = 2)
    private BigDecimal releaseOnlineAmount = BigDecimal.ZERO;

    @Column(name = "release_remarks", length = 500)
    private String releaseRemarks;

    @Column(name = "locker_status", length = 32)
    private String lockerStatus = "LOCKED";

    @Column(name = "created_by", length = 36)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @Version
    private Long version;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getBranchId() { return branchId; }
    public void setBranchId(String branchId) { this.branchId = branchId; }
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public String getTicketNo() { return ticketNo; }
    public void setTicketNo(String ticketNo) { this.ticketNo = ticketNo; }
    public LocalDate getOpenDate() { return openDate; }
    public void setOpenDate(LocalDate openDate) { this.openDate = openDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public BigDecimal getPrincipalAmount() { return principalAmount; }
    public void setPrincipalAmount(BigDecimal principalAmount) { this.principalAmount = principalAmount; }
    public BigDecimal getRoiPercent() { return roiPercent; }
    public void setRoiPercent(BigDecimal roiPercent) { this.roiPercent = roiPercent; }
    public String getInterestType() { return interestType; }
    public void setInterestType(String interestType) { this.interestType = interestType; }
    public String getMetalType() { return metalType; }
    public void setMetalType(String metalType) { this.metalType = metalType; }
    public BigDecimal getGrossWeight() { return grossWeight; }
    public void setGrossWeight(BigDecimal grossWeight) { this.grossWeight = grossWeight; }
    public BigDecimal getNetWeight() { return netWeight; }
    public void setNetWeight(BigDecimal netWeight) { this.netWeight = netWeight; }
    public BigDecimal getFineWeight() { return fineWeight; }
    public void setFineWeight(BigDecimal fineWeight) { this.fineWeight = fineWeight; }
    public BigDecimal getValuationAmount() { return valuationAmount; }
    public void setValuationAmount(BigDecimal valuationAmount) { this.valuationAmount = valuationAmount; }
    public BigDecimal getInterestAmount() { return interestAmount; }
    public void setInterestAmount(BigDecimal interestAmount) { this.interestAmount = interestAmount; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getItemDesc() { return itemDesc; }
    public void setItemDesc(String itemDesc) { this.itemDesc = itemDesc; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public String getPacketNo() { return packetNo; }
    public void setPacketNo(String packetNo) { this.packetNo = packetNo; }
    public String getBoxNo() { return boxNo; }
    public void setBoxNo(String boxNo) { this.boxNo = boxNo; }
    public String getLoanType() { return loanType; }
    public void setLoanType(String loanType) { this.loanType = loanType; }
    public Boolean getFirstMonthInterest() { return firstMonthInterest; }
    public void setFirstMonthInterest(Boolean firstMonthInterest) { this.firstMonthInterest = firstMonthInterest; }
    public Integer getTimePeriodMonths() { return timePeriodMonths; }
    public void setTimePeriodMonths(Integer timePeriodMonths) { this.timePeriodMonths = timePeriodMonths; }
    public String getDrAccount() { return drAccount; }
    public void setDrAccount(String drAccount) { this.drAccount = drAccount; }
    public String getItemLocation() { return itemLocation; }
    public void setItemLocation(String itemLocation) { this.itemLocation = itemLocation; }
    public BigDecimal getCashAmount() { return cashAmount; }
    public void setCashAmount(BigDecimal cashAmount) { this.cashAmount = cashAmount; }
    public String getCashNarration() { return cashNarration; }
    public void setCashNarration(String cashNarration) { this.cashNarration = cashNarration; }
    public BigDecimal getBankAmount() { return bankAmount; }
    public void setBankAmount(BigDecimal bankAmount) { this.bankAmount = bankAmount; }
    public String getBankNarration() { return bankNarration; }
    public void setBankNarration(String bankNarration) { this.bankNarration = bankNarration; }
    public BigDecimal getCardAmount() { return cardAmount; }
    public void setCardAmount(BigDecimal cardAmount) { this.cardAmount = cardAmount; }
    public String getCardNarration() { return cardNarration; }
    public void setCardNarration(String cardNarration) { this.cardNarration = cardNarration; }
    public BigDecimal getOnlineAmount() { return onlineAmount; }
    public void setOnlineAmount(BigDecimal onlineAmount) { this.onlineAmount = onlineAmount; }
    public String getOnlineNarration() { return onlineNarration; }
    public void setOnlineNarration(String onlineNarration) { this.onlineNarration = onlineNarration; }
    public String getItemsJson() { return itemsJson; }
    public void setItemsJson(String itemsJson) { this.itemsJson = itemsJson; }

    public LocalDate getReleaseDate() { return releaseDate; }
    public void setReleaseDate(LocalDate releaseDate) { this.releaseDate = releaseDate; }
    public String getReleaseTicketNo() { return releaseTicketNo; }
    public void setReleaseTicketNo(String releaseTicketNo) { this.releaseTicketNo = releaseTicketNo; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
    public BigDecimal getExtraAmount() { return extraAmount; }
    public void setExtraAmount(BigDecimal extraAmount) { this.extraAmount = extraAmount; }
    public BigDecimal getPaidAmount() { return paidAmount; }
    public void setPaidAmount(BigDecimal paidAmount) { this.paidAmount = paidAmount; }
    public BigDecimal getPaidInterest() { return paidInterest; }
    public void setPaidInterest(BigDecimal paidInterest) { this.paidInterest = paidInterest; }
    public String getMonthlyInterestOption() { return monthlyInterestOption; }
    public void setMonthlyInterestOption(String monthlyInterestOption) { this.monthlyInterestOption = monthlyInterestOption; }
    public BigDecimal getReleaseCashAmount() { return releaseCashAmount; }
    public void setReleaseCashAmount(BigDecimal releaseCashAmount) { this.releaseCashAmount = releaseCashAmount; }
    public BigDecimal getReleaseBankAmount() { return releaseBankAmount; }
    public void setReleaseBankAmount(BigDecimal releaseBankAmount) { this.releaseBankAmount = releaseBankAmount; }
    public BigDecimal getReleaseCardAmount() { return releaseCardAmount; }
    public void setReleaseCardAmount(BigDecimal releaseCardAmount) { this.releaseCardAmount = releaseCardAmount; }
    public BigDecimal getReleaseOnlineAmount() { return releaseOnlineAmount; }
    public void setReleaseOnlineAmount(BigDecimal releaseOnlineAmount) { this.releaseOnlineAmount = releaseOnlineAmount; }
    public String getReleaseRemarks() { return releaseRemarks; }
    public void setReleaseRemarks(String releaseRemarks) { this.releaseRemarks = releaseRemarks; }
    public String getLockerStatus() { return lockerStatus; }
    public void setLockerStatus(String lockerStatus) { this.lockerStatus = lockerStatus; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
