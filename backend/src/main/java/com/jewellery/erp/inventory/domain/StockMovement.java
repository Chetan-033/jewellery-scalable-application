package com.jewellery.erp.inventory.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "stock_movement")
public class StockMovement {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "stock_item_id", nullable = false, length = 36)
    private String stockItemId;

    @Column(name = "branch_id", nullable = false, length = 36)
    private String branchId;

    @Column(name = "movement_type", nullable = false, length = 32)
    private String movementType;

    @Column(nullable = false, precision = 18, scale = 4)
    private BigDecimal quantity;

    @Column(name = "gross_weight", nullable = false, precision = 18, scale = 4)
    private BigDecimal grossWeight = BigDecimal.ZERO;

    @Column(name = "net_weight", nullable = false, precision = 18, scale = 4)
    private BigDecimal netWeight = BigDecimal.ZERO;

    @Column(name = "fine_weight", nullable = false, precision = 18, scale = 4)
    private BigDecimal fineWeight = BigDecimal.ZERO;

    @Column(name = "source_type", nullable = false, length = 64)
    private String sourceType;

    @Column(name = "source_id", length = 64)
    private String sourceId;

    @Column(length = 500)
    private String remarks;

    @Column(name = "created_by", length = 36)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStockItemId() {
        return stockItemId;
    }

    public void setStockItemId(String stockItemId) {
        this.stockItemId = stockItemId;
    }

    public String getBranchId() {
        return branchId;
    }

    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }

    public String getMovementType() {
        return movementType;
    }

    public void setMovementType(String movementType) {
        this.movementType = movementType;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getGrossWeight() {
        return grossWeight;
    }

    public void setGrossWeight(BigDecimal grossWeight) {
        this.grossWeight = grossWeight;
    }

    public BigDecimal getNetWeight() {
        return netWeight;
    }

    public void setNetWeight(BigDecimal netWeight) {
        this.netWeight = netWeight;
    }

    public BigDecimal getFineWeight() {
        return fineWeight;
    }

    public void setFineWeight(BigDecimal fineWeight) {
        this.fineWeight = fineWeight;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
