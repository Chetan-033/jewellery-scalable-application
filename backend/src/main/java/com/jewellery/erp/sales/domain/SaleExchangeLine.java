package com.jewellery.erp.sales.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/** Old-metal exchange taken against a sale. Kept as a flat entity (no list on invoice) to avoid multiple eager bags. */
@Entity
@Table(name = "sale_exchange_line")
public class SaleExchangeLine {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "sales_invoice_id", nullable = false, length = 36)
    private String salesInvoiceId;

    @Column(name = "line_no", nullable = false)
    private int lineNo;

    @Column(name = "metal_type", nullable = false, length = 32)
    private String metalType = "GOLD";

    @Column(length = 255)
    private String description;

    @Column(name = "gross_weight", nullable = false, precision = 18, scale = 4)
    private BigDecimal grossWeight = BigDecimal.ZERO;

    @Column(name = "less_weight", nullable = false, precision = 18, scale = 4)
    private BigDecimal lessWeight = BigDecimal.ZERO;

    @Column(name = "net_weight", nullable = false, precision = 18, scale = 4)
    private BigDecimal netWeight = BigDecimal.ZERO;

    @Column(name = "purity_percent", nullable = false, precision = 10, scale = 4)
    private BigDecimal purityPercent = BigDecimal.ZERO;

    @Column(name = "fine_weight", nullable = false, precision = 18, scale = 4)
    private BigDecimal fineWeight = BigDecimal.ZERO;

    @Column(name = "rate_per_gram", nullable = false, precision = 18, scale = 4)
    private BigDecimal ratePerGram = BigDecimal.ZERO;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal valuation = BigDecimal.ZERO;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getSalesInvoiceId() { return salesInvoiceId; }
    public void setSalesInvoiceId(String salesInvoiceId) { this.salesInvoiceId = salesInvoiceId; }
    public int getLineNo() { return lineNo; }
    public void setLineNo(int lineNo) { this.lineNo = lineNo; }
    public String getMetalType() { return metalType; }
    public void setMetalType(String metalType) { this.metalType = metalType; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getGrossWeight() { return grossWeight; }
    public void setGrossWeight(BigDecimal grossWeight) { this.grossWeight = grossWeight; }
    public BigDecimal getLessWeight() { return lessWeight; }
    public void setLessWeight(BigDecimal lessWeight) { this.lessWeight = lessWeight; }
    public BigDecimal getNetWeight() { return netWeight; }
    public void setNetWeight(BigDecimal netWeight) { this.netWeight = netWeight; }
    public BigDecimal getPurityPercent() { return purityPercent; }
    public void setPurityPercent(BigDecimal purityPercent) { this.purityPercent = purityPercent; }
    public BigDecimal getFineWeight() { return fineWeight; }
    public void setFineWeight(BigDecimal fineWeight) { this.fineWeight = fineWeight; }
    public BigDecimal getRatePerGram() { return ratePerGram; }
    public void setRatePerGram(BigDecimal ratePerGram) { this.ratePerGram = ratePerGram; }
    public BigDecimal getValuation() { return valuation; }
    public void setValuation(BigDecimal valuation) { this.valuation = valuation; }
}
