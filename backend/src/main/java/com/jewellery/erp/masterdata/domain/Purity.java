package com.jewellery.erp.masterdata.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "purity")
public class Purity {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "metal_id", nullable = false, length = 36)
    private String metalId;

    @Column(nullable = false, length = 64)
    private String label;

    @Column(nullable = false, precision = 10, scale = 4)
    private BigDecimal fineness;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

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

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public BigDecimal getFineness() {
        return fineness;
    }

    public void setFineness(BigDecimal fineness) {
        this.fineness = fineness;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
