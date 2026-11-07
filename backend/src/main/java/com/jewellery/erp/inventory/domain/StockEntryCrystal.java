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
@Table(name = "stock_entry_crystal")
public class StockEntryCrystal {

    @Id @Column(length = 36) private String id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_entry_id", nullable = false)
    private StockEntry stockEntry;
    @Column(name = "line_no", nullable = false) private int lineNo;
    @Column(length = 128) private String category;
    @Column(nullable = false) private String name;
    @Column(length = 64) private String clarity;
    @Column(length = 64) private String color;
    @Column(length = 64) private String shape;
    @Column(name = "size_label", length = 64) private String sizeLabel;
    @Column(nullable = false, precision = 18, scale = 4) private BigDecimal quantity = BigDecimal.ONE;
    @Column(nullable = false, precision = 18, scale = 4) private BigDecimal weight = BigDecimal.ZERO;
    @Column(nullable = false, precision = 18, scale = 4) private BigDecimal rate = BigDecimal.ZERO;
    @Column(name = "rate_type", nullable = false, length = 16) private String rateType = "PP";
    @Column(nullable = false, precision = 18, scale = 2) private BigDecimal valuation = BigDecimal.ZERO;
    @Column(name = "auto_less", nullable = false) private boolean autoLess;

    public String getId() { return id; } public void setId(String id) { this.id = id; }
    public StockEntry getStockEntry() { return stockEntry; } public void setStockEntry(StockEntry stockEntry) { this.stockEntry = stockEntry; }
    public int getLineNo() { return lineNo; } public void setLineNo(int lineNo) { this.lineNo = lineNo; }
    public String getCategory() { return category; } public void setCategory(String category) { this.category = category; }
    public String getName() { return name; } public void setName(String name) { this.name = name; }
    public String getClarity() { return clarity; } public void setClarity(String clarity) { this.clarity = clarity; }
    public String getColor() { return color; } public void setColor(String color) { this.color = color; }
    public String getShape() { return shape; } public void setShape(String shape) { this.shape = shape; }
    public String getSizeLabel() { return sizeLabel; } public void setSizeLabel(String sizeLabel) { this.sizeLabel = sizeLabel; }
    public BigDecimal getQuantity() { return quantity; } public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public BigDecimal getWeight() { return weight; } public void setWeight(BigDecimal weight) { this.weight = weight; }
    public BigDecimal getRate() { return rate; } public void setRate(BigDecimal rate) { this.rate = rate; }
    public String getRateType() { return rateType; } public void setRateType(String rateType) { this.rateType = rateType; }
    public BigDecimal getValuation() { return valuation; } public void setValuation(BigDecimal valuation) { this.valuation = valuation; }
    public boolean isAutoLess() { return autoLess; } public void setAutoLess(boolean autoLess) { this.autoLess = autoLess; }
}
