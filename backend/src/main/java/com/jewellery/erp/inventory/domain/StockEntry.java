package com.jewellery.erp.inventory.domain;

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
@Table(name = "stock_entry")
public class StockEntry {

    @Id @Column(length = 36) private String id;
    @Column(name = "branch_id", nullable = false, length = 36) private String branchId;
    @Column(name = "stock_item_id", nullable = false, length = 36) private String stockItemId;
    @Column(name = "entry_no", nullable = false, length = 64) private String entryNo;
    @Column(name = "entry_date", nullable = false) private LocalDate entryDate;
    @Column(name = "stock_type", nullable = false, length = 32) private String stockType = "RETAIL";
    @Column(name = "item_category", length = 128) private String itemCategory;
    @Column(name = "item_name", nullable = false) private String itemName;
    @Column(name = "metal_id", length = 36) private String metalId;
    @Column(name = "purity_id", length = 36) private String purityId;
    @Column(name = "barcode_prefix", length = 32) private String barcodePrefix;
    @Column(length = 64) private String barcode;
    @Column(name = "rfid_no", length = 64) private String rfidNo;
    @Column(name = "hsn_no", length = 32) private String hsnNo;
    @Column(name = "item_size", length = 64) private String itemSize;
    @Column(nullable = false, precision = 18, scale = 4) private BigDecimal quantity = BigDecimal.ONE;
    @Column(name = "gross_weight", nullable = false, precision = 18, scale = 4) private BigDecimal grossWeight = BigDecimal.ZERO;
    @Column(name = "pkt_weight", nullable = false, precision = 18, scale = 4) private BigDecimal pktWeight = BigDecimal.ZERO;
    @Column(name = "less_weight", nullable = false, precision = 18, scale = 4) private BigDecimal lessWeight = BigDecimal.ZERO;
    @Column(name = "tag_weight", nullable = false, precision = 18, scale = 4) private BigDecimal tagWeight = BigDecimal.ZERO;
    @Column(name = "net_weight", nullable = false, precision = 18, scale = 4) private BigDecimal netWeight = BigDecimal.ZERO;
    @Column(name = "purity_percent", nullable = false, precision = 10, scale = 4) private BigDecimal purityPercent = BigDecimal.ZERO;
    @Column(name = "wastage_percent", nullable = false, precision = 10, scale = 4) private BigDecimal wastagePercent = BigDecimal.ZERO;
    @Column(name = "final_purity_percent", nullable = false, precision = 10, scale = 4) private BigDecimal finalPurityPercent = BigDecimal.ZERO;
    @Column(name = "fine_weight", nullable = false, precision = 18, scale = 4) private BigDecimal fineWeight = BigDecimal.ZERO;
    @Column(name = "final_fine_weight", nullable = false, precision = 18, scale = 4) private BigDecimal finalFineWeight = BigDecimal.ZERO;
    @Column(name = "wastage_weight", nullable = false, precision = 18, scale = 4) private BigDecimal wastageWeight = BigDecimal.ZERO;
    @Column(name = "metal_rate", nullable = false, precision = 18, scale = 4) private BigDecimal metalRate = BigDecimal.ZERO;
    @Column(name = "lab_charges", nullable = false, precision = 18, scale = 4) private BigDecimal labCharges = BigDecimal.ZERO;
    @Column(name = "lab_charges_type", nullable = false, length = 16) private String labChargesType = "GM";
    @Column(name = "total_lab_charges", nullable = false, precision = 18, scale = 2) private BigDecimal totalLabCharges = BigDecimal.ZERO;
    @Column(name = "making_charges", nullable = false, precision = 18, scale = 4) private BigDecimal makingCharges = BigDecimal.ZERO;
    @Column(name = "making_charges_type", nullable = false, length = 16) private String makingChargesType = "GM";
    @Column(name = "total_making_charges", nullable = false, precision = 18, scale = 2) private BigDecimal totalMakingCharges = BigDecimal.ZERO;
    @Column(name = "other_charges", nullable = false, precision = 18, scale = 4) private BigDecimal otherCharges = BigDecimal.ZERO;
    @Column(name = "other_charges_type", nullable = false, length = 16) private String otherChargesType = "FX";
    @Column(name = "total_other_charges", nullable = false, precision = 18, scale = 2) private BigDecimal totalOtherCharges = BigDecimal.ZERO;
    @Column(name = "hallmark_charges", nullable = false, precision = 18, scale = 4) private BigDecimal hallmarkCharges = BigDecimal.ZERO;
    @Column(name = "hallmark_charges_type", nullable = false, length = 16) private String hallmarkChargesType = "FX";
    @Column(name = "total_hallmark_charges", nullable = false, precision = 18, scale = 2) private BigDecimal totalHallmarkCharges = BigDecimal.ZERO;
    @Column(name = "stone_valuation", nullable = false, precision = 18, scale = 2) private BigDecimal stoneValuation = BigDecimal.ZERO;
    @Column(name = "metal_valuation", nullable = false, precision = 18, scale = 2) private BigDecimal metalValuation = BigDecimal.ZERO;
    @Column(name = "tax_percent", nullable = false, precision = 10, scale = 4) private BigDecimal taxPercent = BigDecimal.ZERO;
    @Column(name = "tax_amount", nullable = false, precision = 18, scale = 2) private BigDecimal taxAmount = BigDecimal.ZERO;
    @Column(name = "final_valuation", nullable = false, precision = 18, scale = 2) private BigDecimal finalValuation = BigDecimal.ZERO;
    @Column(name = "valuation_by", length = 32) private String valuationBy;
    @Column(name = "making_by", length = 32) private String makingBy;
    @Column(name = "lab_by", length = 32) private String labBy;
    @Column(name = "wastage_by", length = 32) private String wastageBy;
    @Column(name = "final_fine_by", length = 32) private String finalFineBy;
    @Column(name = "fine_base_by", length = 32) private String fineBaseBy;
    @Column(name = "other_info", length = 500) private String otherInfo;
    @Column(nullable = false, length = 32) private String status = "POSTED";
    @Column(name = "movement_id", length = 36) private String movementId;
    @Column(name = "created_by", length = 36) private String createdBy;
    @Column(name = "created_at", nullable = false) private Instant createdAt = Instant.now();
    @Column(name = "updated_at", nullable = false) private Instant updatedAt = Instant.now();
    @Version private Long version;

    @OneToMany(mappedBy = "stockEntry", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("lineNo ASC")
    private List<StockEntryPacket> packets = new ArrayList<>();

    @OneToMany(mappedBy = "stockEntry", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("lineNo ASC")
    private List<StockEntryCrystal> crystals = new ArrayList<>();

    public String getId() { return id; } public void setId(String id) { this.id = id; }
    public String getBranchId() { return branchId; } public void setBranchId(String branchId) { this.branchId = branchId; }
    public String getStockItemId() { return stockItemId; } public void setStockItemId(String stockItemId) { this.stockItemId = stockItemId; }
    public String getEntryNo() { return entryNo; } public void setEntryNo(String entryNo) { this.entryNo = entryNo; }
    public LocalDate getEntryDate() { return entryDate; } public void setEntryDate(LocalDate entryDate) { this.entryDate = entryDate; }
    public String getStockType() { return stockType; } public void setStockType(String stockType) { this.stockType = stockType; }
    public String getItemCategory() { return itemCategory; } public void setItemCategory(String itemCategory) { this.itemCategory = itemCategory; }
    public String getItemName() { return itemName; } public void setItemName(String itemName) { this.itemName = itemName; }
    public String getMetalId() { return metalId; } public void setMetalId(String metalId) { this.metalId = metalId; }
    public String getPurityId() { return purityId; } public void setPurityId(String purityId) { this.purityId = purityId; }
    public String getBarcodePrefix() { return barcodePrefix; } public void setBarcodePrefix(String barcodePrefix) { this.barcodePrefix = barcodePrefix; }
    public String getBarcode() { return barcode; } public void setBarcode(String barcode) { this.barcode = barcode; }
    public String getRfidNo() { return rfidNo; } public void setRfidNo(String rfidNo) { this.rfidNo = rfidNo; }
    public String getHsnNo() { return hsnNo; } public void setHsnNo(String hsnNo) { this.hsnNo = hsnNo; }
    public String getItemSize() { return itemSize; } public void setItemSize(String itemSize) { this.itemSize = itemSize; }
    public BigDecimal getQuantity() { return quantity; } public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public BigDecimal getGrossWeight() { return grossWeight; } public void setGrossWeight(BigDecimal grossWeight) { this.grossWeight = grossWeight; }
    public BigDecimal getPktWeight() { return pktWeight; } public void setPktWeight(BigDecimal pktWeight) { this.pktWeight = pktWeight; }
    public BigDecimal getLessWeight() { return lessWeight; } public void setLessWeight(BigDecimal lessWeight) { this.lessWeight = lessWeight; }
    public BigDecimal getTagWeight() { return tagWeight; } public void setTagWeight(BigDecimal tagWeight) { this.tagWeight = tagWeight; }
    public BigDecimal getNetWeight() { return netWeight; } public void setNetWeight(BigDecimal netWeight) { this.netWeight = netWeight; }
    public BigDecimal getPurityPercent() { return purityPercent; } public void setPurityPercent(BigDecimal purityPercent) { this.purityPercent = purityPercent; }
    public BigDecimal getWastagePercent() { return wastagePercent; } public void setWastagePercent(BigDecimal wastagePercent) { this.wastagePercent = wastagePercent; }
    public BigDecimal getFinalPurityPercent() { return finalPurityPercent; } public void setFinalPurityPercent(BigDecimal finalPurityPercent) { this.finalPurityPercent = finalPurityPercent; }
    public BigDecimal getFineWeight() { return fineWeight; } public void setFineWeight(BigDecimal fineWeight) { this.fineWeight = fineWeight; }
    public BigDecimal getFinalFineWeight() { return finalFineWeight; } public void setFinalFineWeight(BigDecimal finalFineWeight) { this.finalFineWeight = finalFineWeight; }
    public BigDecimal getWastageWeight() { return wastageWeight; } public void setWastageWeight(BigDecimal wastageWeight) { this.wastageWeight = wastageWeight; }
    public BigDecimal getMetalRate() { return metalRate; } public void setMetalRate(BigDecimal metalRate) { this.metalRate = metalRate; }
    public BigDecimal getLabCharges() { return labCharges; } public void setLabCharges(BigDecimal labCharges) { this.labCharges = labCharges; }
    public String getLabChargesType() { return labChargesType; } public void setLabChargesType(String labChargesType) { this.labChargesType = labChargesType; }
    public BigDecimal getTotalLabCharges() { return totalLabCharges; } public void setTotalLabCharges(BigDecimal totalLabCharges) { this.totalLabCharges = totalLabCharges; }
    public BigDecimal getMakingCharges() { return makingCharges; } public void setMakingCharges(BigDecimal makingCharges) { this.makingCharges = makingCharges; }
    public String getMakingChargesType() { return makingChargesType; } public void setMakingChargesType(String makingChargesType) { this.makingChargesType = makingChargesType; }
    public BigDecimal getTotalMakingCharges() { return totalMakingCharges; } public void setTotalMakingCharges(BigDecimal totalMakingCharges) { this.totalMakingCharges = totalMakingCharges; }
    public BigDecimal getOtherCharges() { return otherCharges; } public void setOtherCharges(BigDecimal otherCharges) { this.otherCharges = otherCharges; }
    public String getOtherChargesType() { return otherChargesType; } public void setOtherChargesType(String otherChargesType) { this.otherChargesType = otherChargesType; }
    public BigDecimal getTotalOtherCharges() { return totalOtherCharges; } public void setTotalOtherCharges(BigDecimal totalOtherCharges) { this.totalOtherCharges = totalOtherCharges; }
    public BigDecimal getHallmarkCharges() { return hallmarkCharges; } public void setHallmarkCharges(BigDecimal hallmarkCharges) { this.hallmarkCharges = hallmarkCharges; }
    public String getHallmarkChargesType() { return hallmarkChargesType; } public void setHallmarkChargesType(String hallmarkChargesType) { this.hallmarkChargesType = hallmarkChargesType; }
    public BigDecimal getTotalHallmarkCharges() { return totalHallmarkCharges; } public void setTotalHallmarkCharges(BigDecimal totalHallmarkCharges) { this.totalHallmarkCharges = totalHallmarkCharges; }
    public BigDecimal getStoneValuation() { return stoneValuation; } public void setStoneValuation(BigDecimal stoneValuation) { this.stoneValuation = stoneValuation; }
    public BigDecimal getMetalValuation() { return metalValuation; } public void setMetalValuation(BigDecimal metalValuation) { this.metalValuation = metalValuation; }
    public BigDecimal getTaxPercent() { return taxPercent; } public void setTaxPercent(BigDecimal taxPercent) { this.taxPercent = taxPercent; }
    public BigDecimal getTaxAmount() { return taxAmount; } public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }
    public BigDecimal getFinalValuation() { return finalValuation; } public void setFinalValuation(BigDecimal finalValuation) { this.finalValuation = finalValuation; }
    public String getValuationBy() { return valuationBy; } public void setValuationBy(String valuationBy) { this.valuationBy = valuationBy; }
    public String getMakingBy() { return makingBy; } public void setMakingBy(String makingBy) { this.makingBy = makingBy; }
    public String getLabBy() { return labBy; } public void setLabBy(String labBy) { this.labBy = labBy; }
    public String getWastageBy() { return wastageBy; } public void setWastageBy(String wastageBy) { this.wastageBy = wastageBy; }
    public String getFinalFineBy() { return finalFineBy; } public void setFinalFineBy(String finalFineBy) { this.finalFineBy = finalFineBy; }
    public String getFineBaseBy() { return fineBaseBy; } public void setFineBaseBy(String fineBaseBy) { this.fineBaseBy = fineBaseBy; }
    public String getOtherInfo() { return otherInfo; } public void setOtherInfo(String otherInfo) { this.otherInfo = otherInfo; }
    public String getStatus() { return status; } public void setStatus(String status) { this.status = status; }
    public String getMovementId() { return movementId; } public void setMovementId(String movementId) { this.movementId = movementId; }
    public String getCreatedBy() { return createdBy; } public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public List<StockEntryPacket> getPackets() { return packets; }
    public List<StockEntryCrystal> getCrystals() { return crystals; }
}
