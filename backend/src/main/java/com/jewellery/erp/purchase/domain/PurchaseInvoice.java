package com.jewellery.erp.purchase.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "purchase_invoice")
public class PurchaseInvoice {
    @Id @Column(length = 36) private String id;
    @Column(name = "branch_id", nullable = false, length = 36) private String branchId;
    @Column(name = "supplier_id", length = 36) private String supplierId;
    @Column(name = "invoice_no", nullable = false, length = 64) private String invoiceNo;
    @Column(name = "invoice_date", nullable = false) private LocalDate invoiceDate;
    @Column(nullable = false, length = 32) private String status = "FINALIZED";
    @Column(name = "metal_amount", nullable = false, precision = 18, scale = 2) private BigDecimal metalAmount = BigDecimal.ZERO;
    @Column(name = "making_amount", nullable = false, precision = 18, scale = 2) private BigDecimal makingAmount = BigDecimal.ZERO;
    @Column(name = "stone_amount", nullable = false, precision = 18, scale = 2) private BigDecimal stoneAmount = BigDecimal.ZERO;
    @Column(name = "discount_amount", nullable = false, precision = 18, scale = 2) private BigDecimal discountAmount = BigDecimal.ZERO;
    @Column(name = "taxable_amount", nullable = false, precision = 18, scale = 2) private BigDecimal taxableAmount = BigDecimal.ZERO;
    @Column(name = "tax_amount", nullable = false, precision = 18, scale = 2) private BigDecimal taxAmount = BigDecimal.ZERO;
    @Column(name = "round_off", nullable = false, precision = 18, scale = 2) private BigDecimal roundOff = BigDecimal.ZERO;
    @Column(name = "grand_total", nullable = false, precision = 18, scale = 2) private BigDecimal grandTotal = BigDecimal.ZERO;
    private String remarks;
    @Column(name = "created_by", length = 36) private String createdBy;
    @Column(name = "created_at", nullable = false) private Instant createdAt = Instant.now();
    @Column(name = "updated_at", nullable = false) private Instant updatedAt = Instant.now();
    @Version private Long version;
    @OneToMany(mappedBy = "purchaseInvoice", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("lineNo ASC")
    private List<PurchaseInvoiceLine> lines = new ArrayList<>();

    public String getId() { return id; } public void setId(String id) { this.id = id; }
    public String getBranchId() { return branchId; } public void setBranchId(String branchId) { this.branchId = branchId; }
    public String getSupplierId() { return supplierId; } public void setSupplierId(String supplierId) { this.supplierId = supplierId; }
    public String getInvoiceNo() { return invoiceNo; } public void setInvoiceNo(String invoiceNo) { this.invoiceNo = invoiceNo; }
    public LocalDate getInvoiceDate() { return invoiceDate; } public void setInvoiceDate(LocalDate invoiceDate) { this.invoiceDate = invoiceDate; }
    public String getStatus() { return status; } public void setStatus(String status) { this.status = status; }
    public BigDecimal getMetalAmount() { return metalAmount; } public void setMetalAmount(BigDecimal v) { this.metalAmount = v; }
    public BigDecimal getMakingAmount() { return makingAmount; } public void setMakingAmount(BigDecimal v) { this.makingAmount = v; }
    public BigDecimal getStoneAmount() { return stoneAmount; } public void setStoneAmount(BigDecimal v) { this.stoneAmount = v; }
    public BigDecimal getDiscountAmount() { return discountAmount; } public void setDiscountAmount(BigDecimal v) { this.discountAmount = v; }
    public BigDecimal getTaxableAmount() { return taxableAmount; } public void setTaxableAmount(BigDecimal v) { this.taxableAmount = v; }
    public BigDecimal getTaxAmount() { return taxAmount; } public void setTaxAmount(BigDecimal v) { this.taxAmount = v; }
    public BigDecimal getRoundOff() { return roundOff; } public void setRoundOff(BigDecimal v) { this.roundOff = v; }
    public BigDecimal getGrandTotal() { return grandTotal; } public void setGrandTotal(BigDecimal v) { this.grandTotal = v; }
    public String getRemarks() { return remarks; } public void setRemarks(String remarks) { this.remarks = remarks; }
    public String getCreatedBy() { return createdBy; } public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public List<PurchaseInvoiceLine> getLines() { return lines; }
}
