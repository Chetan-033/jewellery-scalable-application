package com.jewellery.erp.loans.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "girvi_loan_item")
public class GirviLoanItem {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "girvi_loan_id", nullable = false, length = 36)
    private String girviLoanId;

    @Column(name = "line_no", nullable = false)
    private Integer lineNo = 1;

    @Column(name = "metal_type", nullable = false, length = 32)
    private String metalType = "GOLD";

    @Column(name = "item_name", nullable = false)
    private String itemName;

    @Column(nullable = false)
    private Integer quantity = 1;

    @Column(name = "gross_weight", nullable = false, precision = 18, scale = 4)
    private BigDecimal grossWeight = BigDecimal.ZERO;

    @Column(name = "net_weight", nullable = false, precision = 18, scale = 4)
    private BigDecimal netWeight = BigDecimal.ZERO;

    @Column(name = "less_weight", nullable = false, precision = 18, scale = 4)
    private BigDecimal lessWeight = BigDecimal.ZERO;

    @Column(name = "purity_percent", nullable = false, precision = 10, scale = 4)
    private BigDecimal purityPercent = BigDecimal.ZERO;

    @Column(name = "fine_weight", nullable = false, precision = 18, scale = 4)
    private BigDecimal fineWeight = BigDecimal.ZERO;

    @Column(name = "valuation_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal valuationAmount = BigDecimal.ZERO;

    @Column(name = "photo_url")
    private String photoUrl;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getGirviLoanId() { return girviLoanId; }
    public void setGirviLoanId(String girviLoanId) { this.girviLoanId = girviLoanId; }
    public Integer getLineNo() { return lineNo; }
    public void setLineNo(Integer lineNo) { this.lineNo = lineNo; }
    public String getMetalType() { return metalType; }
    public void setMetalType(String metalType) { this.metalType = metalType; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public BigDecimal getGrossWeight() { return grossWeight; }
    public void setGrossWeight(BigDecimal grossWeight) { this.grossWeight = grossWeight; }
    public BigDecimal getNetWeight() { return netWeight; }
    public void setNetWeight(BigDecimal netWeight) { this.netWeight = netWeight; }
    public BigDecimal getLessWeight() { return lessWeight; }
    public void setLessWeight(BigDecimal lessWeight) { this.lessWeight = lessWeight; }
    public BigDecimal getPurityPercent() { return purityPercent; }
    public void setPurityPercent(BigDecimal purityPercent) { this.purityPercent = purityPercent; }
    public BigDecimal getFineWeight() { return fineWeight; }
    public void setFineWeight(BigDecimal fineWeight) { this.fineWeight = fineWeight; }
    public BigDecimal getValuationAmount() { return valuationAmount; }
    public void setValuationAmount(BigDecimal valuationAmount) { this.valuationAmount = valuationAmount; }
    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
}

