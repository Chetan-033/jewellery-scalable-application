package com.jewellery.erp.inventory.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public final class InventoryDtos {

    private InventoryDtos() {
    }

    public record CreateStockItemRequest(
            @NotBlank @Size(max = 64) String sku,
            @NotBlank @Size(max = 255) String name,
            String metalId,
            String purityId,
            String stockMode,
            String category,
            String stockType,
            String barcodePrefix,
            String barcode,
            String rfidNo,
            String hsnNo,
            String itemSize,
            String otherInfo
    ) {
    }

    public record StockItemResponse(
            String id,
            String sku,
            String name,
            String metalId,
            String purityId,
            String stockMode,
            String status,
            String category,
            String stockType,
            String barcode,
            String rfidNo
    ) {
    }

    public record StockLookupResponse(
            String id,
            String sku,
            String name,
            String metalId,
            String purityId,
            BigDecimal purityPercent,
            String stockMode,
            String status,
            String category,
            String stockType,
            String barcode,
            String rfidNo,
            BigDecimal availableQuantity,
            BigDecimal availableGrossWeight,
            BigDecimal availableNetWeight
    ) {
    }

    public record StockSummaryResponse(
            BigDecimal goldGross,
            BigDecimal goldNet,
            BigDecimal silverGross,
            BigDecimal silverNet,
            long itemCount,
            long retailCount
    ) {
    }

    public record PostMovementRequest(
            @NotBlank String stockItemId,
            @NotBlank String branchId,
            @NotBlank String movementType,
            @NotNull BigDecimal quantity,
            BigDecimal grossWeight,
            BigDecimal netWeight,
            BigDecimal fineWeight,
            @NotBlank String sourceType,
            String sourceId,
            String remarks
    ) {
    }

    public record StockBalanceResponse(
            String stockItemId,
            String branchId,
            BigDecimal quantity,
            BigDecimal grossWeight,
            BigDecimal netWeight,
            BigDecimal fineWeight
    ) {
    }

    public record StockMovementResponse(
            String id,
            String stockItemId,
            String branchId,
            String movementType,
            BigDecimal quantity,
            BigDecimal grossWeight,
            BigDecimal netWeight,
            BigDecimal fineWeight,
            String sourceType,
            String sourceId,
            StockBalanceResponse resultingBalance
    ) {
    }

    public record PacketLineRequest(
            String description,
            BigDecimal quantity,
            BigDecimal weight,
            String operation
    ) {
    }

    public record CrystalLineRequest(
            String category,
            @NotBlank String name,
            String clarity,
            String color,
            String shape,
            String sizeLabel,
            BigDecimal quantity,
            BigDecimal weight,
            BigDecimal rate,
            String rateType,
            boolean autoLess
    ) {
    }

    public record AddStockRequest(
            @NotBlank String branchId,
            String stockItemId,
            String sku,
            @NotBlank String itemName,
            String itemCategory,
            String stockType,
            String metalId,
            String purityId,
            String barcodePrefix,
            String barcode,
            String rfidNo,
            String hsnNo,
            String itemSize,
            String otherInfo,
            LocalDate entryDate,
            @NotNull BigDecimal quantity,
            @NotNull BigDecimal grossWeight,
            BigDecimal pktWeight,
            BigDecimal lessWeight,
            BigDecimal tagWeight,
            BigDecimal purityPercent,
            BigDecimal wastagePercent,
            BigDecimal metalRate,
            BigDecimal labCharges,
            String labChargesType,
            BigDecimal makingCharges,
            String makingChargesType,
            BigDecimal otherCharges,
            String otherChargesType,
            BigDecimal hallmarkCharges,
            String hallmarkChargesType,
            BigDecimal taxPercent,
            @Valid List<PacketLineRequest> packets,
            @Valid List<CrystalLineRequest> crystals
    ) {
    }

    public record PacketLineResponse(
            String id,
            int lineNo,
            String description,
            BigDecimal quantity,
            BigDecimal weight,
            String operation
    ) {
    }

    public record CrystalLineResponse(
            String id,
            int lineNo,
            String category,
            String name,
            String clarity,
            String color,
            String shape,
            String sizeLabel,
            BigDecimal quantity,
            BigDecimal weight,
            BigDecimal rate,
            String rateType,
            BigDecimal valuation,
            boolean autoLess
    ) {
    }

    public record StockEntryResponse(
            String id,
            String entryNo,
            LocalDate entryDate,
            String branchId,
            String stockItemId,
            String stockType,
            String itemCategory,
            String itemName,
            String metalId,
            String purityId,
            String barcode,
            String rfidNo,
            BigDecimal quantity,
            BigDecimal grossWeight,
            BigDecimal pktWeight,
            BigDecimal lessWeight,
            BigDecimal tagWeight,
            BigDecimal netWeight,
            BigDecimal purityPercent,
            BigDecimal wastagePercent,
            BigDecimal finalPurityPercent,
            BigDecimal fineWeight,
            BigDecimal wastageWeight,
            BigDecimal finalFineWeight,
            BigDecimal metalRate,
            BigDecimal metalValuation,
            BigDecimal totalLabCharges,
            BigDecimal totalMakingCharges,
            BigDecimal totalOtherCharges,
            BigDecimal totalHallmarkCharges,
            BigDecimal stoneValuation,
            BigDecimal taxAmount,
            BigDecimal finalValuation,
            String valuationBy,
            String makingBy,
            String labBy,
            String wastageBy,
            String finalFineBy,
            String status,
            String movementId,
            List<PacketLineResponse> packets,
            List<CrystalLineResponse> crystals
    ) {
    }

    public record StockPreviewRequest(
            BigDecimal quantity,
            @NotNull BigDecimal grossWeight,
            BigDecimal pktWeight,
            BigDecimal lessWeight,
            @NotNull BigDecimal purityPercent,
            BigDecimal wastagePercent,
            @NotNull BigDecimal metalRate,
            BigDecimal labCharges,
            String labChargesType,
            BigDecimal makingCharges,
            String makingChargesType,
            BigDecimal otherCharges,
            String otherChargesType,
            BigDecimal hallmarkCharges,
            String hallmarkChargesType,
            BigDecimal taxPercent,
            List<CrystalLineRequest> crystals
    ) {
    }
}
