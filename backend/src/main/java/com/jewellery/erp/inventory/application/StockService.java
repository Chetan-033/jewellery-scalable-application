package com.jewellery.erp.inventory.application;

import com.jewellery.erp.inventory.api.InventoryDtos;
import com.jewellery.erp.inventory.domain.StockBalance;
import com.jewellery.erp.inventory.domain.StockItem;
import com.jewellery.erp.inventory.domain.StockMovement;
import com.jewellery.erp.inventory.repository.StockBalanceRepository;
import com.jewellery.erp.inventory.repository.StockItemRepository;
import com.jewellery.erp.inventory.repository.StockMovementRepository;
import com.jewellery.erp.masterdata.domain.Purity;
import com.jewellery.erp.masterdata.repository.PurityRepository;
import com.jewellery.erp.organization.repository.BranchRepository;
import com.jewellery.erp.shared.domain.BusinessException;
import com.jewellery.erp.shared.domain.MoneyMath;
import com.jewellery.erp.shared.domain.NotFoundException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
public class StockService {

    private final StockItemRepository stockItemRepository;
    private final StockBalanceRepository stockBalanceRepository;
    private final StockMovementRepository stockMovementRepository;
    private final BranchRepository branchRepository;
    private final PurityRepository purityRepository;

    public StockService(
            StockItemRepository stockItemRepository,
            StockBalanceRepository stockBalanceRepository,
            StockMovementRepository stockMovementRepository,
            BranchRepository branchRepository,
            PurityRepository purityRepository) {
        this.stockItemRepository = stockItemRepository;
        this.stockBalanceRepository = stockBalanceRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.branchRepository = branchRepository;
        this.purityRepository = purityRepository;
    }

    @Transactional
    public InventoryDtos.StockItemResponse createItem(InventoryDtos.CreateStockItemRequest request) {
        if (stockItemRepository.existsBySku(request.sku())) {
            throw new BusinessException("SKU_EXISTS", "Stock SKU already exists");
        }
        StockItem item = new StockItem();
        item.setId(UUID.randomUUID().toString());
        item.setSku(request.sku().trim());
        item.setName(request.name().trim());
        item.setMetalId(request.metalId());
        item.setPurityId(request.purityId());
        item.setStockMode(request.stockMode() == null ? "QUANTITY_WEIGHT" : request.stockMode());
        item.setCategory(request.category());
        item.setStockType(request.stockType() == null ? "RETAIL" : request.stockType().toUpperCase());
        item.setBarcodePrefix(request.barcodePrefix());
        item.setBarcode(request.barcode());
        item.setRfidNo(request.rfidNo());
        item.setHsnNo(request.hsnNo());
        item.setItemSize(request.itemSize());
        item.setOtherInfo(request.otherInfo());
        stockItemRepository.save(item);
        return toItem(item);
    }

    @Transactional(readOnly = true)
    public List<InventoryDtos.StockItemResponse> listItems() {
        return stockItemRepository.findAll().stream().map(this::toItem).toList();
    }

    /** Search by sku / barcode / rfid / name (exact matches ranked first), with available stock totals. */
    @Transactional(readOnly = true)
    public List<InventoryDtos.StockLookupResponse> lookupItems(String q) {
        if (q == null || q.isBlank()) {
            return List.of();
        }
        List<StockItem> items = stockItemRepository.lookup(q.trim(), PageRequest.of(0, 25));
        if (items.isEmpty()) {
            return List.of();
        }
        List<String> ids = items.stream().map(StockItem::getId).toList();
        Map<String, BigDecimal[]> totals = new HashMap<>();
        for (StockBalance b : stockBalanceRepository.findByStockItemIdIn(ids)) {
            BigDecimal[] t = totals.computeIfAbsent(b.getStockItemId(),
                    k -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO});
            t[0] = t[0].add(b.getQuantity());
            t[1] = t[1].add(b.getGrossWeight());
            t[2] = t[2].add(b.getNetWeight());
        }
        List<String> purityIds = items.stream().map(StockItem::getPurityId).filter(Objects::nonNull).distinct().toList();
        Map<String, BigDecimal> fineness = new HashMap<>();
        purityRepository.findAllById(purityIds).forEach(p -> fineness.put(p.getId(), p.getFineness()));

        return items.stream().map(i -> {
            BigDecimal[] t = totals.getOrDefault(i.getId(),
                    new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO});
            return new InventoryDtos.StockLookupResponse(
                    i.getId(), i.getSku(), i.getName(), i.getMetalId(), i.getPurityId(),
                    i.getPurityId() == null ? null : fineness.get(i.getPurityId()),
                    i.getStockMode(), i.getStatus(), i.getCategory(), i.getStockType(),
                    i.getBarcode(), i.getRfidNo(),
                    MoneyMath.weight(t[0]), MoneyMath.weight(t[1]), MoneyMath.weight(t[2])
            );
        }).toList();
    }

    @Transactional
    public InventoryDtos.StockMovementResponse postMovement(InventoryDtos.PostMovementRequest request, String actorUserId) {
        StockItem item = stockItemRepository.findById(request.stockItemId())
                .orElseThrow(() -> new NotFoundException("Stock item not found"));
        branchRepository.findById(request.branchId())
                .orElseThrow(() -> new NotFoundException("Branch not found"));

        BigDecimal qty = MoneyMath.weight(request.quantity());
        BigDecimal gross = MoneyMath.weight(nullToZero(request.grossWeight()));
        BigDecimal net = MoneyMath.weight(nullToZero(request.netWeight()));
        BigDecimal fine = request.fineWeight() != null
                ? MoneyMath.weight(request.fineWeight())
                : computeFine(item.getPurityId(), net);

        String type = request.movementType().toUpperCase();
        if (!List.of("IN", "OUT").contains(type)) {
            throw new BusinessException("INVALID_MOVEMENT", "movementType must be IN or OUT");
        }

        StockBalance balance = stockBalanceRepository
                .findByStockItemIdAndBranchId(item.getId(), request.branchId())
                .orElseGet(() -> newBalance(item.getId(), request.branchId()));

        if ("IN".equals(type)) {
            balance.setQuantity(MoneyMath.weight(balance.getQuantity().add(qty)));
            balance.setGrossWeight(MoneyMath.weight(balance.getGrossWeight().add(gross)));
            balance.setNetWeight(MoneyMath.weight(balance.getNetWeight().add(net)));
            balance.setFineWeight(MoneyMath.weight(balance.getFineWeight().add(fine)));
        } else {
            if (balance.getQuantity().compareTo(qty) < 0) {
                throw new BusinessException("NEGATIVE_STOCK", "Insufficient quantity for outward movement");
            }
            balance.setQuantity(MoneyMath.weight(balance.getQuantity().subtract(qty)));
            balance.setGrossWeight(MoneyMath.weight(balance.getGrossWeight().subtract(gross)));
            balance.setNetWeight(MoneyMath.weight(balance.getNetWeight().subtract(net)));
            balance.setFineWeight(MoneyMath.weight(balance.getFineWeight().subtract(fine)));
        }
        balance.setUpdatedAt(Instant.now());
        stockBalanceRepository.save(balance);

        StockMovement movement = new StockMovement();
        movement.setId(UUID.randomUUID().toString());
        movement.setStockItemId(item.getId());
        movement.setBranchId(request.branchId());
        movement.setMovementType(type);
        movement.setQuantity(qty);
        movement.setGrossWeight(gross);
        movement.setNetWeight(net);
        movement.setFineWeight(fine);
        movement.setSourceType(request.sourceType());
        movement.setSourceId(request.sourceId());
        movement.setRemarks(request.remarks());
        movement.setCreatedBy(actorUserId);
        stockMovementRepository.save(movement);

        return toMovement(movement, balance);
    }

    @Transactional(readOnly = true)
    public InventoryDtos.StockBalanceResponse getBalance(String stockItemId, String branchId) {
        StockBalance balance = stockBalanceRepository.findByStockItemIdAndBranchId(stockItemId, branchId)
                .orElseThrow(() -> new NotFoundException("Stock balance not found"));
        return new InventoryDtos.StockBalanceResponse(
                balance.getStockItemId(),
                balance.getBranchId(),
                balance.getQuantity(),
                balance.getGrossWeight(),
                balance.getNetWeight(),
                balance.getFineWeight()
        );
    }

    private BigDecimal computeFine(String purityId, BigDecimal net) {
        if (purityId == null || net.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO.setScale(MoneyMath.WEIGHT_SCALE);
        }
        Purity purity = purityRepository.findById(purityId)
                .orElseThrow(() -> new NotFoundException("Purity not found"));
        return MoneyMath.fineWeight(net, purity.getFineness());
    }

    private StockBalance newBalance(String stockItemId, String branchId) {
        StockBalance balance = new StockBalance();
        balance.setId(UUID.randomUUID().toString());
        balance.setStockItemId(stockItemId);
        balance.setBranchId(branchId);
        return balance;
    }

    private BigDecimal nullToZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private InventoryDtos.StockItemResponse toItem(StockItem item) {
        return new InventoryDtos.StockItemResponse(
                item.getId(),
                item.getSku(),
                item.getName(),
                item.getMetalId(),
                item.getPurityId(),
                item.getStockMode(),
                item.getStatus(),
                item.getCategory(),
                item.getStockType(),
                item.getBarcode(),
                item.getRfidNo()
        );
    }

    private InventoryDtos.StockMovementResponse toMovement(StockMovement movement, StockBalance balance) {
        return new InventoryDtos.StockMovementResponse(
                movement.getId(),
                movement.getStockItemId(),
                movement.getBranchId(),
                movement.getMovementType(),
                movement.getQuantity(),
                movement.getGrossWeight(),
                movement.getNetWeight(),
                movement.getFineWeight(),
                movement.getSourceType(),
                movement.getSourceId(),
                new InventoryDtos.StockBalanceResponse(
                        balance.getStockItemId(),
                        balance.getBranchId(),
                        balance.getQuantity(),
                        balance.getGrossWeight(),
                        balance.getNetWeight(),
                        balance.getFineWeight()
                )
        );
    }
}
