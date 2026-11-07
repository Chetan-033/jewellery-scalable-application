package com.jewellery.erp.inventory.api;

import com.jewellery.erp.calculation.StockAddResult;
import com.jewellery.erp.inventory.application.AddStockService;
import com.jewellery.erp.inventory.application.StockService;
import com.jewellery.erp.inventory.application.StockSummaryService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {

    private final StockService stockService;
    private final AddStockService addStockService;
    private final StockSummaryService stockSummaryService;

    public InventoryController(
            StockService stockService,
            AddStockService addStockService,
            StockSummaryService stockSummaryService) {
        this.stockService = stockService;
        this.addStockService = addStockService;
        this.stockSummaryService = stockSummaryService;
    }

    @GetMapping("/summary")
    public InventoryDtos.StockSummaryResponse summary() {
        return stockSummaryService.summary();
    }

    @GetMapping("/items/lookup")
    public List<InventoryDtos.StockLookupResponse> lookup(@RequestParam String q) {
        return stockService.lookupItems(q);
    }

    @PostMapping("/items")
    public InventoryDtos.StockItemResponse createItem(@Valid @RequestBody InventoryDtos.CreateStockItemRequest request) {
        return stockService.createItem(request);
    }

    @GetMapping("/items")
    public List<InventoryDtos.StockItemResponse> items() {
        return stockService.listItems();
    }

    @PostMapping("/movements")
    public InventoryDtos.StockMovementResponse postMovement(
            @Valid @RequestBody InventoryDtos.PostMovementRequest request,
            Authentication authentication) {
        String actor = authentication == null ? null : String.valueOf(authentication.getPrincipal());
        return stockService.postMovement(request, actor);
    }

    @GetMapping("/balances")
    public InventoryDtos.StockBalanceResponse balance(
            @RequestParam String stockItemId,
            @RequestParam String branchId) {
        return stockService.getBalance(stockItemId, branchId);
    }

    @PostMapping("/add-stock/preview")
    public StockAddResult previewAddStock(@Valid @RequestBody InventoryDtos.StockPreviewRequest request) {
        return addStockService.preview(request);
    }

    @PostMapping("/add-stock")
    public InventoryDtos.StockEntryResponse addStock(
            @Valid @RequestBody InventoryDtos.AddStockRequest request,
            Authentication authentication) {
        String actor = authentication == null ? null : String.valueOf(authentication.getPrincipal());
        return addStockService.addStock(request, actor);
    }

    @GetMapping("/add-stock")
    public List<InventoryDtos.StockEntryResponse> listAddStock() {
        return addStockService.listEntries();
    }
}
