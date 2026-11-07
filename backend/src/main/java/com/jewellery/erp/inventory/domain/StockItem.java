package com.jewellery.erp.inventory.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;

@Entity
@Table(name = "stock_item")
public class StockItem {

    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false, unique = true, length = 64)
    private String sku;

    @Column(nullable = false)
    private String name;

    @Column(name = "metal_id", length = 36)
    private String metalId;

    @Column(name = "purity_id", length = 36)
    private String purityId;

    @Column(length = 128)
    private String category;

    @Column(name = "stock_type", nullable = false, length = 32)
    private String stockType = "RETAIL";

    @Column(name = "barcode_prefix", length = 32)
    private String barcodePrefix;

    @Column(length = 64)
    private String barcode;

    @Column(name = "rfid_no", length = 64)
    private String rfidNo;

    @Column(name = "hsn_no", length = 32)
    private String hsnNo;

    @Column(name = "item_size", length = 64)
    private String itemSize;

    @Column(name = "other_info", length = 500)
    private String otherInfo;

    @Column(name = "stock_mode", nullable = false, length = 32)
    private String stockMode = "QUANTITY_WEIGHT";

    @Column(nullable = false, length = 32)
    private String status = "ACTIVE";

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @Version
    private Long version;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getStockType() {
        return stockType;
    }

    public void setStockType(String stockType) {
        this.stockType = stockType;
    }

    public String getBarcodePrefix() {
        return barcodePrefix;
    }

    public void setBarcodePrefix(String barcodePrefix) {
        this.barcodePrefix = barcodePrefix;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getRfidNo() {
        return rfidNo;
    }

    public void setRfidNo(String rfidNo) {
        this.rfidNo = rfidNo;
    }

    public String getHsnNo() {
        return hsnNo;
    }

    public void setHsnNo(String hsnNo) {
        this.hsnNo = hsnNo;
    }

    public String getItemSize() {
        return itemSize;
    }

    public void setItemSize(String itemSize) {
        this.itemSize = itemSize;
    }

    public String getOtherInfo() {
        return otherInfo;
    }

    public void setOtherInfo(String otherInfo) {
        this.otherInfo = otherInfo;
    }

    public String getStockMode() {
        return stockMode;
    }

    public void setStockMode(String stockMode) {
        this.stockMode = stockMode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getVersion() {
        return version;
    }
}
