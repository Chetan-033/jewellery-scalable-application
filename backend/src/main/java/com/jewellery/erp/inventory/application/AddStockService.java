package com.jewellery.erp.inventory.application;

import com.jewellery.erp.calculation.StockAddInput;
import com.jewellery.erp.calculation.StockAddResult;
import com.jewellery.erp.calculation.StockAddSettings;
import com.jewellery.erp.calculation.StockAddValuationEngine;
import com.jewellery.erp.calculation.application.CalcSettingService;
import com.jewellery.erp.inventory.api.InventoryDtos;
import com.jewellery.erp.inventory.domain.StockEntry;
import com.jewellery.erp.inventory.domain.StockEntryCrystal;
import com.jewellery.erp.inventory.domain.StockEntryPacket;
import com.jewellery.erp.inventory.domain.StockItem;
import com.jewellery.erp.inventory.repository.StockEntryRepository;
import com.jewellery.erp.inventory.repository.StockItemRepository;
import com.jewellery.erp.masterdata.domain.Purity;
import com.jewellery.erp.masterdata.repository.PurityRepository;
import com.jewellery.erp.organization.application.DocumentNumberService;
import com.jewellery.erp.organization.repository.BranchRepository;
import com.jewellery.erp.shared.api.PageRequests;
import com.jewellery.erp.shared.api.PageResponse;
import com.jewellery.erp.shared.domain.BusinessException;
import com.jewellery.erp.shared.domain.MoneyMath;
import com.jewellery.erp.shared.domain.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AddStockService {

    private final StockEntryRepository stockEntryRepository;
    private final StockItemRepository stockItemRepository;
    private final BranchRepository branchRepository;
    private final PurityRepository purityRepository;
    private final StockService stockService;
    private final DocumentNumberService documentNumberService;
    private final StockAddValuationEngine valuationEngine;
    private final CalcSettingService calcSettingService;

    public AddStockService(
            StockEntryRepository stockEntryRepository,
            StockItemRepository stockItemRepository,
            BranchRepository branchRepository,
            PurityRepository purityRepository,
            StockService stockService,
            DocumentNumberService documentNumberService,
            StockAddValuationEngine valuationEngine,
            CalcSettingService calcSettingService) {
        this.stockEntryRepository = stockEntryRepository;
        this.stockItemRepository = stockItemRepository;
        this.branchRepository = branchRepository;
        this.purityRepository = purityRepository;
        this.stockService = stockService;
        this.documentNumberService = documentNumberService;
        this.valuationEngine = valuationEngine;
        this.calcSettingService = calcSettingService;
    }

    @Transactional(readOnly = true)
    public StockAddResult preview(InventoryDtos.StockPreviewRequest request) {
        return valuationEngine.calculate(toInput(request, calcSettingService.stockAddSettings()));
    }

    @Transactional
    public InventoryDtos.StockEntryResponse addStock(InventoryDtos.AddStockRequest request, String actorUserId) {
        branchRepository.findById(request.branchId())
                .orElseThrow(() -> new NotFoundException("Branch not found"));

        BigDecimal purityPercent = resolvePurityPercent(request);
        BigDecimal pktWeight = MoneyMath.weight(nz(request.pktWeight()));
        BigDecimal lessWeight = MoneyMath.weight(nz(request.lessWeight()).add(summarizePacketLess(request.packets())));

        StockAddResult calc = valuationEngine.calculate(new StockAddInput(
                request.quantity(),
                request.grossWeight(),
                pktWeight,
                lessWeight,
                purityPercent,
                nz(request.wastagePercent()),
                nz(request.metalRate()),
                nz(request.labCharges()),
                request.labChargesType(),
                nz(request.makingCharges()),
                request.makingChargesType(),
                nz(request.otherCharges()),
                request.otherChargesType(),
                nz(request.hallmarkCharges()),
                request.hallmarkChargesType(),
                nz(request.taxPercent()),
                toCrystalInputs(request.crystals()),
                calcSettingService.stockAddSettings()
        ));

        StockItem item = resolveOrCreateItem(request);

        InventoryDtos.StockMovementResponse movement = stockService.postMovement(
                new InventoryDtos.PostMovementRequest(
                        item.getId(),
                        request.branchId(),
                        "IN",
                        request.quantity(),
                        calc.grossWeight(),
                        calc.netWeight(),
                        calc.finalFineWeight(),
                        "ADD_STOCK",
                        null,
                        "Add stock " + item.getSku()
                ),
                actorUserId
        );

        StockEntry entry = new StockEntry();
        entry.setId(UUID.randomUUID().toString());
        entry.setBranchId(request.branchId());
        entry.setStockItemId(item.getId());
        entry.setEntryNo(documentNumberService.next("STOCK"));
        entry.setEntryDate(request.entryDate() == null ? LocalDate.now() : request.entryDate());
        entry.setStockType(blankTo(request.stockType(), "RETAIL").toUpperCase());
        entry.setItemCategory(request.itemCategory());
        entry.setItemName(request.itemName().trim());
        entry.setMetalId(request.metalId());
        entry.setPurityId(request.purityId());
        entry.setBarcodePrefix(request.barcodePrefix());
        entry.setBarcode(request.barcode());
        entry.setRfidNo(request.rfidNo());
        entry.setHsnNo(request.hsnNo());
        entry.setItemSize(request.itemSize());
        entry.setOtherInfo(request.otherInfo());
        entry.setQuantity(MoneyMath.weight(request.quantity()));
        entry.setGrossWeight(calc.grossWeight());
        entry.setPktWeight(calc.pktWeight());
        entry.setLessWeight(calc.lessWeight());
        entry.setTagWeight(MoneyMath.weight(nz(request.tagWeight())));
        entry.setNetWeight(calc.netWeight());
        entry.setPurityPercent(calc.purityPercent());
        entry.setWastagePercent(calc.wastagePercent());
        entry.setFinalPurityPercent(calc.finalPurityPercent());
        entry.setFineWeight(calc.fineWeight());
        entry.setFinalFineWeight(calc.finalFineWeight());
        entry.setWastageWeight(calc.wastageWeight());
        entry.setMetalRate(calc.metalRatePerGram());
        entry.setLabCharges(MoneyMath.rate(nz(request.labCharges())));
        entry.setLabChargesType(blankTo(request.labChargesType(), "GM"));
        entry.setTotalLabCharges(calc.totalLabCharges());
        entry.setMakingCharges(MoneyMath.rate(nz(request.makingCharges())));
        entry.setMakingChargesType(blankTo(request.makingChargesType(), "GM"));
        entry.setTotalMakingCharges(calc.totalMakingCharges());
        entry.setOtherCharges(MoneyMath.rate(nz(request.otherCharges())));
        entry.setOtherChargesType(blankTo(request.otherChargesType(), "FX"));
        entry.setTotalOtherCharges(calc.totalOtherCharges());
        entry.setHallmarkCharges(MoneyMath.rate(nz(request.hallmarkCharges())));
        entry.setHallmarkChargesType(blankTo(request.hallmarkChargesType(), "FX"));
        entry.setTotalHallmarkCharges(calc.totalHallmarkCharges());
        entry.setStoneValuation(calc.stoneValuation());
        entry.setMetalValuation(calc.metalValuation());
        entry.setTaxPercent(MoneyMath.purity(nz(request.taxPercent())));
        entry.setTaxAmount(calc.taxAmount());
        entry.setFinalValuation(calc.finalValuation());
        entry.setValuationBy(calc.valuationBy());
        entry.setMakingBy(calc.makingBy());
        entry.setLabBy(calc.labBy());
        entry.setWastageBy(calc.wastageBy());
        entry.setFinalFineBy(calc.finalFineBy());
        entry.setFineBaseBy(calc.fineBaseBy());
        entry.setMovementId(movement.id());
        entry.setCreatedBy(actorUserId);

        int pNo = 1;
        if (request.packets() != null) {
            for (InventoryDtos.PacketLineRequest row : request.packets()) {
                if (row == null || (isBlank(row.description()) && nz(row.weight()).compareTo(BigDecimal.ZERO) == 0)) {
                    continue;
                }
                StockEntryPacket packet = new StockEntryPacket();
                packet.setId(UUID.randomUUID().toString());
                packet.setStockEntry(entry);
                packet.setLineNo(pNo++);
                packet.setDescription(row.description());
                packet.setQuantity(MoneyMath.weight(nz(row.quantity(), BigDecimal.ONE)));
                packet.setWeight(MoneyMath.weight(nz(row.weight())));
                packet.setOperation(blankTo(row.operation(), "LESS").toUpperCase());
                entry.getPackets().add(packet);
            }
        }

        int cNo = 1;
        if (request.crystals() != null) {
            for (InventoryDtos.CrystalLineRequest row : request.crystals()) {
                if (row == null || isBlank(row.name())) {
                    continue;
                }
                BigDecimal cQty = MoneyMath.weight(nz(row.quantity(), BigDecimal.ONE));
                BigDecimal cWt = MoneyMath.weight(nz(row.weight()));
                BigDecimal cRate = MoneyMath.rate(nz(row.rate()));
                BigDecimal cVal = MoneyMath.money(valuationEngine.chargeAmount(
                        cRate, row.rateType(), cQty, cWt, BigDecimal.ZERO));
                StockEntryCrystal crystal = new StockEntryCrystal();
                crystal.setId(UUID.randomUUID().toString());
                crystal.setStockEntry(entry);
                crystal.setLineNo(cNo++);
                crystal.setCategory(row.category());
                crystal.setName(row.name().trim());
                crystal.setClarity(row.clarity());
                crystal.setColor(row.color());
                crystal.setShape(row.shape());
                crystal.setSizeLabel(row.sizeLabel());
                crystal.setQuantity(cQty);
                crystal.setWeight(cWt);
                crystal.setRate(cRate);
                crystal.setRateType(blankTo(row.rateType(), "PP"));
                crystal.setValuation(cVal);
                crystal.setAutoLess(row.autoLess());
                entry.getCrystals().add(crystal);
            }
        }

        // Patch movement source id now that entry exists
        stockEntryRepository.save(entry);
        return toResponse(entry);
    }

    @Transactional(readOnly = true)
    public PageResponse<InventoryDtos.StockEntryResponse> listEntries(Integer page, Integer size) {
        Page<StockEntry> rows = stockEntryRepository.findAllBy(
                PageRequests.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        );
        return PageResponse.from(rows.map(this::toResponse));
    }

    private StockItem resolveOrCreateItem(InventoryDtos.AddStockRequest request) {
        if (request.stockItemId() != null && !request.stockItemId().isBlank()) {
            StockItem existing = stockItemRepository.findById(request.stockItemId())
                    .orElseThrow(() -> new NotFoundException("Stock item not found"));
            applyItemMeta(existing, request);
            return stockItemRepository.save(existing);
        }

        String sku = request.sku() == null || request.sku().isBlank()
                ? documentNumberService.next("STOCK")
                : request.sku().trim();
        if (stockItemRepository.existsBySku(sku)) {
            throw new BusinessException("SKU_EXISTS", "Stock SKU already exists: " + sku);
        }
        StockItem item = new StockItem();
        item.setId(UUID.randomUUID().toString());
        item.setSku(sku);
        item.setName(request.itemName().trim());
        item.setMetalId(request.metalId());
        item.setPurityId(request.purityId());
        item.setStockMode("QUANTITY_WEIGHT");
        applyItemMeta(item, request);
        return stockItemRepository.save(item);
    }

    private void applyItemMeta(StockItem item, InventoryDtos.AddStockRequest request) {
        if (request.itemCategory() != null) {
            item.setCategory(request.itemCategory());
        }
        if (request.stockType() != null) {
            item.setStockType(request.stockType().toUpperCase());
        }
        if (request.barcodePrefix() != null) {
            item.setBarcodePrefix(request.barcodePrefix());
        }
        if (request.barcode() != null) {
            item.setBarcode(request.barcode());
        }
        if (request.rfidNo() != null) {
            item.setRfidNo(request.rfidNo());
        }
        if (request.hsnNo() != null) {
            item.setHsnNo(request.hsnNo());
        }
        if (request.itemSize() != null) {
            item.setItemSize(request.itemSize());
        }
        if (request.otherInfo() != null) {
            item.setOtherInfo(request.otherInfo());
        }
        if (request.metalId() != null) {
            item.setMetalId(request.metalId());
        }
        if (request.purityId() != null) {
            item.setPurityId(request.purityId());
        }
        if (request.itemName() != null && !request.itemName().isBlank()) {
            item.setName(request.itemName().trim());
        }
    }

    private BigDecimal resolvePurityPercent(InventoryDtos.AddStockRequest request) {
        if (request.purityPercent() != null) {
            return request.purityPercent();
        }
        if (request.purityId() != null && !request.purityId().isBlank()) {
            Purity purity = purityRepository.findById(request.purityId())
                    .orElseThrow(() -> new NotFoundException("Purity not found"));
            return purity.getFineness();
        }
        throw new BusinessException("PURITY_REQUIRED", "purityPercent or purityId is required");
    }

    private StockAddInput toInput(InventoryDtos.StockPreviewRequest request, StockAddSettings settings) {
        return new StockAddInput(
                request.quantity(),
                request.grossWeight(),
                request.pktWeight(),
                request.lessWeight(),
                request.purityPercent(),
                request.wastagePercent(),
                request.metalRate(),
                request.labCharges(),
                request.labChargesType(),
                request.makingCharges(),
                request.makingChargesType(),
                request.otherCharges(),
                request.otherChargesType(),
                request.hallmarkCharges(),
                request.hallmarkChargesType(),
                request.taxPercent(),
                toCrystalInputs(request.crystals()),
                settings
        );
    }

    private List<StockAddInput.CrystalLine> toCrystalInputs(List<InventoryDtos.CrystalLineRequest> crystals) {
        if (crystals == null) {
            return List.of();
        }
        List<StockAddInput.CrystalLine> lines = new ArrayList<>();
        for (InventoryDtos.CrystalLineRequest c : crystals) {
            if (c == null) {
                continue;
            }
            lines.add(new StockAddInput.CrystalLine(
                    c.quantity(), c.weight(), c.rate(), c.rateType(), c.autoLess()));
        }
        return lines;
    }

    private BigDecimal summarizePacketLess(List<InventoryDtos.PacketLineRequest> packets) {
        BigDecimal less = BigDecimal.ZERO;
        if (packets == null) {
            return less;
        }
        for (InventoryDtos.PacketLineRequest row : packets) {
            if (row == null) {
                continue;
            }
            BigDecimal rowWt = MoneyMath.weight(nz(row.quantity(), BigDecimal.ONE).multiply(nz(row.weight())));
            String op = blankTo(row.operation(), "LESS").toUpperCase();
            // Reference: ADD operation reduces total less; LESS adds to less
            if ("ADD".equals(op)) {
                less = less.subtract(rowWt);
            } else {
                less = less.add(rowWt);
            }
        }
        if (less.compareTo(BigDecimal.ZERO) < 0) {
            less = BigDecimal.ZERO;
        }
        return MoneyMath.weight(less);
    }

    private InventoryDtos.StockEntryResponse toResponse(StockEntry entry) {
        return new InventoryDtos.StockEntryResponse(
                entry.getId(),
                entry.getEntryNo(),
                entry.getEntryDate(),
                entry.getBranchId(),
                entry.getStockItemId(),
                entry.getStockType(),
                entry.getItemCategory(),
                entry.getItemName(),
                entry.getMetalId(),
                entry.getPurityId(),
                entry.getBarcode(),
                entry.getRfidNo(),
                entry.getQuantity(),
                entry.getGrossWeight(),
                entry.getPktWeight(),
                entry.getLessWeight(),
                entry.getTagWeight(),
                entry.getNetWeight(),
                entry.getPurityPercent(),
                entry.getWastagePercent(),
                entry.getFinalPurityPercent(),
                entry.getFineWeight(),
                entry.getWastageWeight(),
                entry.getFinalFineWeight(),
                entry.getMetalRate(),
                entry.getMetalValuation(),
                entry.getTotalLabCharges(),
                entry.getTotalMakingCharges(),
                entry.getTotalOtherCharges(),
                entry.getTotalHallmarkCharges(),
                entry.getStoneValuation(),
                entry.getTaxAmount(),
                entry.getFinalValuation(),
                entry.getValuationBy(),
                entry.getMakingBy(),
                entry.getLabBy(),
                entry.getWastageBy(),
                entry.getFinalFineBy(),
                entry.getStatus(),
                entry.getMovementId(),
                entry.getPackets().stream()
                        .map(p -> new InventoryDtos.PacketLineResponse(
                                p.getId(), p.getLineNo(), p.getDescription(), p.getQuantity(), p.getWeight(), p.getOperation()))
                        .toList(),
                entry.getCrystals().stream()
                        .map(c -> new InventoryDtos.CrystalLineResponse(
                                c.getId(), c.getLineNo(), c.getCategory(), c.getName(), c.getClarity(), c.getColor(),
                                c.getShape(), c.getSizeLabel(), c.getQuantity(), c.getWeight(), c.getRate(),
                                c.getRateType(), c.getValuation(), c.isAutoLess()))
                        .toList()
        );
    }

    private BigDecimal nz(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private BigDecimal nz(BigDecimal value, BigDecimal fallback) {
        return value == null ? fallback : value;
    }

    private String blankTo(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

}
