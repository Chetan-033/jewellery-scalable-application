package com.jewellery.erp.inventory.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "stock_entry_packet")
public class StockEntryPacket {

    @Id @Column(length = 36) private String id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_entry_id", nullable = false)
    private StockEntry stockEntry;
    @Column(name = "line_no", nullable = false) private int lineNo;
    @Column(length = 255) private String description;
    @Column(nullable = false, precision = 18, scale = 4) private BigDecimal quantity = BigDecimal.ONE;
    @Column(nullable = false, precision = 18, scale = 4) private BigDecimal weight = BigDecimal.ZERO;
    @Column(nullable = false, length = 8) private String operation = "LESS";

    public String getId() { return id; } public void setId(String id) { this.id = id; }
    public StockEntry getStockEntry() { return stockEntry; } public void setStockEntry(StockEntry stockEntry) { this.stockEntry = stockEntry; }
    public int getLineNo() { return lineNo; } public void setLineNo(int lineNo) { this.lineNo = lineNo; }
    public String getDescription() { return description; } public void setDescription(String description) { this.description = description; }
    public BigDecimal getQuantity() { return quantity; } public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public BigDecimal getWeight() { return weight; } public void setWeight(BigDecimal weight) { this.weight = weight; }
    public String getOperation() { return operation; } public void setOperation(String operation) { this.operation = operation; }
}
