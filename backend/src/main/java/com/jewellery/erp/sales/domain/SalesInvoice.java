package com.jewellery.erp.sales.domain;

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
@Table(name = "sales_invoice")
public class SalesInvoice {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "branch_id", nullable = false, length = 36)
    private String branchId;

    @Column(name = "customer_id", length = 36)
    private String customerId;

    @Column(name = "invoice_no", nullable = false, length = 64)
    private String invoiceNo;

    @Column(name = "invoice_date", nullable = false)
    private LocalDate invoiceDate;

    @Column(nullable = false, length = 32)
    private String status = "DRAFT";

    @Column(name = "metal_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal metalAmount = BigDecimal.ZERO;

    @Column(name = "making_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal makingAmount = BigDecimal.ZERO;

    @Column(name = "stone_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal stoneAmount = BigDecimal.ZERO;

    @Column(name = "discount_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "taxable_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal taxableAmount = BigDecimal.ZERO;

    @Column(name = "cgst_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal cgstAmount = BigDecimal.ZERO;

    @Column(name = "sgst_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal sgstAmount = BigDecimal.ZERO;

    @Column(name = "igst_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal igstAmount = BigDecimal.ZERO;

    @Column(name = "tax_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "round_off", nullable = false, precision = 18, scale = 2)
    private BigDecimal roundOff = BigDecimal.ZERO;

    @Column(name = "grand_total", nullable = false, precision = 18, scale = 2)
    private BigDecimal grandTotal = BigDecimal.ZERO;

    @Column(name = "paid_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Column(name = "cash_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal cashAmount = BigDecimal.ZERO;

    @Column(name = "card_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal cardAmount = BigDecimal.ZERO;

    @Column(name = "online_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal onlineAmount = BigDecimal.ZERO;

    @Column(name = "cheque_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal chequeAmount = BigDecimal.ZERO;

    @Column(name = "udhaar_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal udhaarAmount = BigDecimal.ZERO;

    @Column(name = "exchange_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal exchangeAmount = BigDecimal.ZERO;

    @Column(name = "hallmark_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal hallmarkAmount = BigDecimal.ZERO;

    @Column(name = "other_charges", nullable = false, precision = 18, scale = 2)
    private BigDecimal otherCharges = BigDecimal.ZERO;

    @Column(name = "courier_charges", nullable = false, precision = 18, scale = 2)
    private BigDecimal courierCharges = BigDecimal.ZERO;

    private String remarks;

    @Column(name = "calc_snapshot", columnDefinition = "TEXT")
    private String calcSnapshot;

    @Column(name = "created_by", length = 36)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @Version
    private Long version;

    @OneToMany(mappedBy = "salesInvoice", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("lineNo ASC")
    private List<SalesInvoiceLine> lines = new ArrayList<>();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getBranchId() { return branchId; }
    public void setBranchId(String branchId) { this.branchId = branchId; }
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public String getInvoiceNo() { return invoiceNo; }
    public void setInvoiceNo(String invoiceNo) { this.invoiceNo = invoiceNo; }
    public LocalDate getInvoiceDate() { return invoiceDate; }
    public void setInvoiceDate(LocalDate invoiceDate) { this.invoiceDate = invoiceDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public BigDecimal getMetalAmount() { return metalAmount; }
    public void setMetalAmount(BigDecimal metalAmount) { this.metalAmount = metalAmount; }
    public BigDecimal getMakingAmount() { return makingAmount; }
    public void setMakingAmount(BigDecimal makingAmount) { this.makingAmount = makingAmount; }
    public BigDecimal getStoneAmount() { return stoneAmount; }
    public void setStoneAmount(BigDecimal stoneAmount) { this.stoneAmount = stoneAmount; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
    public BigDecimal getTaxableAmount() { return taxableAmount; }
    public void setTaxableAmount(BigDecimal taxableAmount) { this.taxableAmount = taxableAmount; }
    public BigDecimal getCgstAmount() { return cgstAmount; }
    public void setCgstAmount(BigDecimal cgstAmount) { this.cgstAmount = cgstAmount; }
    public BigDecimal getSgstAmount() { return sgstAmount; }
    public void setSgstAmount(BigDecimal sgstAmount) { this.sgstAmount = sgstAmount; }
    public BigDecimal getIgstAmount() { return igstAmount; }
    public void setIgstAmount(BigDecimal igstAmount) { this.igstAmount = igstAmount; }
    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }
    public BigDecimal getRoundOff() { return roundOff; }
    public void setRoundOff(BigDecimal roundOff) { this.roundOff = roundOff; }
    public BigDecimal getGrandTotal() { return grandTotal; }
    public void setGrandTotal(BigDecimal grandTotal) { this.grandTotal = grandTotal; }
    public BigDecimal getPaidAmount() { return paidAmount; }
    public void setPaidAmount(BigDecimal paidAmount) { this.paidAmount = paidAmount; }
    public BigDecimal getCashAmount() { return cashAmount; }
    public void setCashAmount(BigDecimal cashAmount) { this.cashAmount = cashAmount; }
    public BigDecimal getCardAmount() { return cardAmount; }
    public void setCardAmount(BigDecimal cardAmount) { this.cardAmount = cardAmount; }
    public BigDecimal getOnlineAmount() { return onlineAmount; }
    public void setOnlineAmount(BigDecimal onlineAmount) { this.onlineAmount = onlineAmount; }
    public BigDecimal getChequeAmount() { return chequeAmount; }
    public void setChequeAmount(BigDecimal chequeAmount) { this.chequeAmount = chequeAmount; }
    public BigDecimal getUdhaarAmount() { return udhaarAmount; }
    public void setUdhaarAmount(BigDecimal udhaarAmount) { this.udhaarAmount = udhaarAmount; }
    public BigDecimal getExchangeAmount() { return exchangeAmount; }
    public void setExchangeAmount(BigDecimal exchangeAmount) { this.exchangeAmount = exchangeAmount; }
    public BigDecimal getHallmarkAmount() { return hallmarkAmount; }
    public void setHallmarkAmount(BigDecimal hallmarkAmount) { this.hallmarkAmount = hallmarkAmount; }
    public BigDecimal getOtherCharges() { return otherCharges; }
    public void setOtherCharges(BigDecimal otherCharges) { this.otherCharges = otherCharges; }
    public BigDecimal getCourierCharges() { return courierCharges; }
    public void setCourierCharges(BigDecimal courierCharges) { this.courierCharges = courierCharges; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
    public String getCalcSnapshot() { return calcSnapshot; }
    public void setCalcSnapshot(String calcSnapshot) { this.calcSnapshot = calcSnapshot; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public List<SalesInvoiceLine> getLines() { return lines; }
    public void setLines(List<SalesInvoiceLine> lines) { this.lines = lines; }
}
