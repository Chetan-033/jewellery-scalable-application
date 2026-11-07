package com.jewellery.erp.purchase.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "purchase_invoice_line")
public class PurchaseInvoiceLine {
    @Id @Column(length = 36) private String id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "purchase_invoice_id", nullable = false)
    private PurchaseInvoice purchaseInvoice;
    @Column(name = "line_no", nullable = false) private int lineNo;
    @Column(name = "stock_item_id", length = 36) private String stockItemId;
    @Column(name = "item_name", nullable = false) private String itemName;
    @Column(name = "metal_id", length = 36) private String metalId;
    @Column(name = "purity_id", length = 36) private String purityId;
    @Column(nullable = false, precision = 18, scale = 4) private BigDecimal quantity = BigDecimal.ONE;
    @Column(name = "gross_weight", nullable = false, precision = 18, scale = 4) private BigDecimal grossWeight = BigDecimal.ZERO;
    @Column(name = "stone_weight", nullable = false, precision = 18, scale = 4) private BigDecimal stoneWeight = BigDecimal.ZERO;
    @Column(name = "net_weight", nullable = false, precision = 18, scale = 4) private BigDecimal netWeight = BigDecimal.ZERO;
    @Column(name = "purity_percent", nullable = false, precision = 10, scale = 4) private BigDecimal purityPercent = BigDecimal.ZERO;
    @Column(name = "wastage_percent", nullable = false, precision = 10, scale = 4) private BigDecimal wastagePercent = BigDecimal.ZERO;
    @Column(name = "fine_weight", nullable = false, precision = 18, scale = 4) private BigDecimal fineWeight = BigDecimal.ZERO;
    @Column(name = "final_fine_weight", nullable = false, precision = 18, scale = 4) private BigDecimal finalFineWeight = BigDecimal.ZERO;
    @Column(name = "rate_per_gram", nullable = false, precision = 18, scale = 4) private BigDecimal ratePerGram = BigDecimal.ZERO;
    @Column(name = "metal_amount", nullable = false, precision = 18, scale = 2) private BigDecimal metalAmount = BigDecimal.ZERO;
    @Column(name = "making_type", nullable = false, length = 32) private String makingType = "PER_GRAM";
    @Column(name = "making_rate", nullable = false, precision = 18, scale = 4) private BigDecimal makingRate = BigDecimal.ZERO;
    @Column(name = "making_amount", nullable = false, precision = 18, scale = 2) private BigDecimal makingAmount = BigDecimal.ZERO;
    @Column(name = "stone_amount", nullable = false, precision = 18, scale = 2) private BigDecimal stoneAmount = BigDecimal.ZERO;
    @Column(name = "line_total", nullable = false, precision = 18, scale = 2) private BigDecimal lineTotal = BigDecimal.ZERO;

    public String getId() { return id; } public void setId(String id) { this.id = id; }
    public PurchaseInvoice getPurchaseInvoice() { return purchaseInvoice; } public void setPurchaseInvoice(PurchaseInvoice p) { this.purchaseInvoice = p; }
    public int getLineNo() { return lineNo; } public void setLineNo(int lineNo) { this.lineNo = lineNo; }
    public String getStockItemId() { return stockItemId; } public void setStockItemId(String stockItemId) { this.stockItemId = stockItemId; }
    public String getItemName() { return itemName; } public void setItemName(String itemName) { this.itemName = itemName; }
    public String getMetalId() { return metalId; } public void setMetalId(String metalId) { this.metalId = metalId; }
    public String getPurityId() { return purityId; } public void setPurityId(String purityId) { this.purityId = purityId; }
    public BigDecimal getQuantity() { return quantity; } public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public BigDecimal getGrossWeight() { return grossWeight; } public void setGrossWeight(BigDecimal v) { this.grossWeight = v; }
    public BigDecimal getStoneWeight() { return stoneWeight; } public void setStoneWeight(BigDecimal v) { this.stoneWeight = v; }
    public BigDecimal getNetWeight() { return netWeight; } public void setNetWeight(BigDecimal v) { this.netWeight = v; }
    public BigDecimal getPurityPercent() { return purityPercent; } public void setPurityPercent(BigDecimal v) { this.purityPercent = v; }
    public BigDecimal getWastagePercent() { return wastagePercent; } public void setWastagePercent(BigDecimal v) { this.wastagePercent = v; }
    public BigDecimal getFineWeight() { return fineWeight; } public void setFineWeight(BigDecimal v) { this.fineWeight = v; }
    public BigDecimal getFinalFineWeight() { return finalFineWeight; } public void setFinalFineWeight(BigDecimal v) { this.finalFineWeight = v; }
    public BigDecimal getRatePerGram() { return ratePerGram; } public void setRatePerGram(BigDecimal v) { this.ratePerGram = v; }
    public BigDecimal getMetalAmount() { return metalAmount; } public void setMetalAmount(BigDecimal v) { this.metalAmount = v; }
    public String getMakingType() { return makingType; } public void setMakingType(String makingType) { this.makingType = makingType; }
    public BigDecimal getMakingRate() { return makingRate; } public void setMakingRate(BigDecimal v) { this.makingRate = v; }
    public BigDecimal getMakingAmount() { return makingAmount; } public void setMakingAmount(BigDecimal v) { this.makingAmount = v; }
    public BigDecimal getStoneAmount() { return stoneAmount; } public void setStoneAmount(BigDecimal v) { this.stoneAmount = v; }
    public BigDecimal getLineTotal() { return lineTotal; } public void setLineTotal(BigDecimal v) { this.lineTotal = v; }
}
