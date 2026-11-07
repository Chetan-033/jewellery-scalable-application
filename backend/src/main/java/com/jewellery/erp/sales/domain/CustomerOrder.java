package com.jewellery.erp.sales.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "customer_order")
public class CustomerOrder {
    @Id @Column(length = 36) private String id;
    @Column(name = "branch_id", nullable = false, length = 36) private String branchId;
    @Column(name = "customer_id", length = 36) private String customerId;
    @Column(name = "order_no", nullable = false, length = 64) private String orderNo;
    @Column(name = "order_date", nullable = false) private LocalDate orderDate;
    @Column(name = "due_date") private LocalDate dueDate;
    @Column(nullable = false, length = 32) private String status = "OPEN";
    @Column(name = "item_name", nullable = false) private String itemName;
    @Column(name = "metal_id", length = 36) private String metalId;
    @Column(name = "purity_id", length = 36) private String purityId;
    @Column(name = "target_weight", precision = 18, scale = 4) private BigDecimal targetWeight;
    @Column(name = "estimated_amount", precision = 18, scale = 2) private BigDecimal estimatedAmount;
    @Column(name = "advance_amount", nullable = false, precision = 18, scale = 2) private BigDecimal advanceAmount = BigDecimal.ZERO;
    private String remarks;
    @Column(name = "created_by", length = 36) private String createdBy;
    @Column(name = "created_at", nullable = false) private Instant createdAt = Instant.now();
    @Column(name = "updated_at", nullable = false) private Instant updatedAt = Instant.now();

    public String getId() { return id; } public void setId(String id) { this.id = id; }
    public String getBranchId() { return branchId; } public void setBranchId(String branchId) { this.branchId = branchId; }
    public String getCustomerId() { return customerId; } public void setCustomerId(String customerId) { this.customerId = customerId; }
    public String getOrderNo() { return orderNo; } public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public LocalDate getOrderDate() { return orderDate; } public void setOrderDate(LocalDate orderDate) { this.orderDate = orderDate; }
    public LocalDate getDueDate() { return dueDate; } public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public String getStatus() { return status; } public void setStatus(String status) { this.status = status; }
    public String getItemName() { return itemName; } public void setItemName(String itemName) { this.itemName = itemName; }
    public String getMetalId() { return metalId; } public void setMetalId(String metalId) { this.metalId = metalId; }
    public String getPurityId() { return purityId; } public void setPurityId(String purityId) { this.purityId = purityId; }
    public BigDecimal getTargetWeight() { return targetWeight; } public void setTargetWeight(BigDecimal targetWeight) { this.targetWeight = targetWeight; }
    public BigDecimal getEstimatedAmount() { return estimatedAmount; } public void setEstimatedAmount(BigDecimal estimatedAmount) { this.estimatedAmount = estimatedAmount; }
    public BigDecimal getAdvanceAmount() { return advanceAmount; } public void setAdvanceAmount(BigDecimal advanceAmount) { this.advanceAmount = advanceAmount; }
    public String getRemarks() { return remarks; } public void setRemarks(String remarks) { this.remarks = remarks; }
    public String getCreatedBy() { return createdBy; } public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
}
