package com.jewellery.erp.sales.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "sales_invoice_line")
public class SalesInvoiceLine {

    @Id
    @Column(length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_invoice_id", nullable = false)
    private SalesInvoice salesInvoice;

    @Column(name = "line_no", nullable = false)
    private int lineNo;

    @Column(name = "stock_item_id", length = 36)
    private String stockItemId;

    @Column(name = "item_name", nullable = false)
    private String itemName;

    @Column(name = "metal_id", length = 36)
    private String metalId;

    @Column(name = "purity_id", length = 36)
    private String purityId;

    @Column(nullable = false, precision = 18, scale = 4)
    private BigDecimal quantity = BigDecimal.ONE;

    @Column(name = "gross_weight", nullable = false, precision = 18, scale = 4)
    private BigDecimal grossWeight = BigDecimal.ZERO;

    @Column(name = "stone_weight", nullable = false, precision = 18, scale = 4)
    private BigDecimal stoneWeight = BigDecimal.ZERO;

    @Column(name = "net_weight", nullable = false, precision = 18, scale = 4)
    private BigDecimal netWeight = BigDecimal.ZERO;

    @Column(name = "purity_percent", nullable = false, precision = 10, scale = 4)
    private BigDecimal purityPercent = BigDecimal.ZERO;

    @Column(name = "wastage_percent", nullable = false, precision = 10, scale = 4)
    private BigDecimal wastagePercent = BigDecimal.ZERO;

    @Column(name = "fine_weight", nullable = false, precision = 18, scale = 4)
    private BigDecimal fineWeight = BigDecimal.ZERO;

    @Column(name = "final_fine_weight", nullable = false, precision = 18, scale = 4)
    private BigDecimal finalFineWeight = BigDecimal.ZERO;

    @Column(name = "rate_per_gram", nullable = false, precision = 18, scale = 4)
    private BigDecimal ratePerGram = BigDecimal.ZERO;

    @Column(name = "metal_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal metalAmount = BigDecimal.ZERO;

    @Column(name = "making_type", nullable = false, length = 32)
    private String makingType = "PER_GRAM";

    @Column(name = "making_rate", nullable = false, precision = 18, scale = 4)
    private BigDecimal makingRate = BigDecimal.ZERO;

    @Column(name = "making_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal makingAmount = BigDecimal.ZERO;

    @Column(name = "stone_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal stoneAmount = BigDecimal.ZERO;

    @Column(name = "discount_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "line_total", nullable = false, precision = 18, scale = 2)
    private BigDecimal lineTotal = BigDecimal.ZERO;

    @Column(length = 64)
    private String barcode;

    @Column(name = "cust_wastage_percent", nullable = false, precision = 10, scale = 4)
    private BigDecimal custWastagePercent = BigDecimal.ZERO;

    @Column(name = "value_added", nullable = false, precision = 18, scale = 2)
    private BigDecimal valueAdded = BigDecimal.ZERO;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public SalesInvoice getSalesInvoice() { return salesInvoice; }
    public void setSalesInvoice(SalesInvoice salesInvoice) { this.salesInvoice = salesInvoice; }
    public int getLineNo() { return lineNo; }
    public void setLineNo(int lineNo) { this.lineNo = lineNo; }
    public String getStockItemId() { return stockItemId; }
    public void setStockItemId(String stockItemId) { this.stockItemId = stockItemId; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public String getMetalId() { return metalId; }
    public void setMetalId(String metalId) { this.metalId = metalId; }
    public String getPurityId() { return purityId; }
    public void setPurityId(String purityId) { this.purityId = purityId; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public BigDecimal getGrossWeight() { return grossWeight; }
    public void setGrossWeight(BigDecimal grossWeight) { this.grossWeight = grossWeight; }
    public BigDecimal getStoneWeight() { return stoneWeight; }
    public void setStoneWeight(BigDecimal stoneWeight) { this.stoneWeight = stoneWeight; }
    public BigDecimal getNetWeight() { return netWeight; }
    public void setNetWeight(BigDecimal netWeight) { this.netWeight = netWeight; }
    public BigDecimal getPurityPercent() { return purityPercent; }
    public void setPurityPercent(BigDecimal purityPercent) { this.purityPercent = purityPercent; }
    public BigDecimal getWastagePercent() { return wastagePercent; }
    public void setWastagePercent(BigDecimal wastagePercent) { this.wastagePercent = wastagePercent; }
    public BigDecimal getFineWeight() { return fineWeight; }
    public void setFineWeight(BigDecimal fineWeight) { this.fineWeight = fineWeight; }
    public BigDecimal getFinalFineWeight() { return finalFineWeight; }
    public void setFinalFineWeight(BigDecimal finalFineWeight) { this.finalFineWeight = finalFineWeight; }
    public BigDecimal getRatePerGram() { return ratePerGram; }
    public void setRatePerGram(BigDecimal ratePerGram) { this.ratePerGram = ratePerGram; }
    public BigDecimal getMetalAmount() { return metalAmount; }
    public void setMetalAmount(BigDecimal metalAmount) { this.metalAmount = metalAmount; }
    public String getMakingType() { return makingType; }
    public void setMakingType(String makingType) { this.makingType = makingType; }
    public BigDecimal getMakingRate() { return makingRate; }
    public void setMakingRate(BigDecimal makingRate) { this.makingRate = makingRate; }
    public BigDecimal getMakingAmount() { return makingAmount; }
    public void setMakingAmount(BigDecimal makingAmount) { this.makingAmount = makingAmount; }
    public BigDecimal getStoneAmount() { return stoneAmount; }
    public void setStoneAmount(BigDecimal stoneAmount) { this.stoneAmount = stoneAmount; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
    public BigDecimal getLineTotal() { return lineTotal; }
    public void setLineTotal(BigDecimal lineTotal) { this.lineTotal = lineTotal; }
    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }
    public BigDecimal getCustWastagePercent() { return custWastagePercent; }
    public void setCustWastagePercent(BigDecimal custWastagePercent) { this.custWastagePercent = custWastagePercent; }
    public BigDecimal getValueAdded() { return valueAdded; }
    public void setValueAdded(BigDecimal valueAdded) { this.valueAdded = valueAdded; }
}
