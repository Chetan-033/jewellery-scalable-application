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
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
