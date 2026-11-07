package com.jewellery.erp.rates.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "metal_rate")
public class MetalRate {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "metal_id", nullable = false, length = 36)
    private String metalId;

    @Column(name = "purity_id", length = 36)
    private String purityId;

    @Column(name = "branch_id", length = 36)
    private String branchId;

    @Column(name = "rate_per_gram", nullable = false, precision = 18, scale = 4)
    private BigDecimal ratePerGram;

    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode = "INR";

    @Column(name = "effective_from", nullable = false)
    private Instant effectiveFrom;

    @Column(name = "effective_to")
    private Instant effectiveTo;

    @Column(nullable = false, length = 64)
    private String source = "MANUAL";

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

    public String getMetalId() {
        return metalId;
    }

    public void setMetalId(String metalId) {
        this.metalId = metalId;
    }

    public String getPurityId() {
        return purityId;
    }

    public void setPurityId(String purityId) {
        this.purityId = purityId;
    }

    public String getBranchId() {
        return branchId;
    }

    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }

    public BigDecimal getRatePerGram() {
        return ratePerGram;
    }

    public void setRatePerGram(BigDecimal ratePerGram) {
        this.ratePerGram = ratePerGram;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public Instant getEffectiveFrom() {
        return effectiveFrom;
    }

    public void setEffectiveFrom(Instant effectiveFrom) {
        this.effectiveFrom = effectiveFrom;
    }

    public Instant getEffectiveTo() {
        return effectiveTo;
    }

    public void setEffectiveTo(Instant effectiveTo) {
        this.effectiveTo = effectiveTo;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}
