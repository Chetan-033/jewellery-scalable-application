package com.jewellery.erp.purchase.api;

import com.jewellery.erp.calculation.MetalLineInput;
import com.jewellery.erp.calculation.MetalLineResult;
import com.jewellery.erp.calculation.MetalValuationEngine;
import com.jewellery.erp.inventory.api.InventoryDtos;
import com.jewellery.erp.inventory.application.StockService;
import com.jewellery.erp.organization.application.DocumentNumberService;
import com.jewellery.erp.organization.repository.BranchRepository;
import com.jewellery.erp.purchase.domain.PurchaseInvoice;
import com.jewellery.erp.purchase.domain.PurchaseInvoiceLine;
import com.jewellery.erp.purchase.repository.PurchaseInvoiceRepository;
import com.jewellery.erp.shared.domain.MoneyMath;
import com.jewellery.erp.shared.domain.NotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/purchase")
public class PurchaseController {

    private final PurchaseInvoiceRepository repository;
    private final DocumentNumberService numbers;
    private final MetalValuationEngine engine;
    private final BranchRepository branchRepository;
    private final StockService stockService;

    public PurchaseController(
            PurchaseInvoiceRepository repository,
            DocumentNumberService numbers,
            MetalValuationEngine engine,
            BranchRepository branchRepository,
            StockService stockService) {
        this.repository = repository;
        this.numbers = numbers;
        this.engine = engine;
        this.branchRepository = branchRepository;
        this.stockService = stockService;
    }

    @PostMapping("/invoices")
    @Transactional
    public PurchaseResponse create(@Valid @RequestBody CreatePurchaseRequest request, Authentication auth) {
        branchRepository.findById(request.branchId()).orElseThrow(() -> new NotFoundException("Branch not found"));
        String actor = auth == null ? null : String.valueOf(auth.getPrincipal());

        PurchaseInvoice invoice = new PurchaseInvoice();
        invoice.setId(UUID.randomUUID().toString());
        invoice.setBranchId(request.branchId());
        invoice.setSupplierId(request.supplierId());
        invoice.setInvoiceNo(numbers.next("PURCHASE"));
        invoice.setInvoiceDate(request.invoiceDate() == null ? LocalDate.now() : request.invoiceDate());
        invoice.setRemarks(request.remarks());
        invoice.setCreatedBy(actor);

        BigDecimal metal = BigDecimal.ZERO;
        BigDecimal making = BigDecimal.ZERO;
        BigDecimal stone = BigDecimal.ZERO;
        BigDecimal taxable = BigDecimal.ZERO;
        BigDecimal tax = BigDecimal.ZERO;
        int n = 1;
        for (LineRequest lineReq : request.lines()) {
            MetalLineResult calc = engine.calculate(new MetalLineInput(
                    lineReq.quantity(), lineReq.grossWeight(), lineReq.stoneWeight(), lineReq.netWeight(),
                    lineReq.purityPercent(), lineReq.wastagePercent(), lineReq.ratePerGram(),
                    lineReq.makingType(), lineReq.makingRate(), lineReq.stoneAmount(), BigDecimal.ZERO,
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO
            ));
            PurchaseInvoiceLine line = new PurchaseInvoiceLine();
            line.setId(UUID.randomUUID().toString());
            line.setPurchaseInvoice(invoice);
            line.setLineNo(n++);
            line.setStockItemId(lineReq.stockItemId());
            line.setItemName(lineReq.itemName());
            line.setMetalId(lineReq.metalId());
            line.setPurityId(lineReq.purityId());
            line.setQuantity(lineReq.quantity() == null ? BigDecimal.ONE : lineReq.quantity());
            line.setGrossWeight(calc.grossWeight());
            line.setStoneWeight(calc.stoneWeight());
            line.setNetWeight(calc.netWeight());
            line.setPurityPercent(calc.purityPercent());
            line.setWastagePercent(calc.wastagePercent());
            line.setFineWeight(calc.fineWeight());
            line.setFinalFineWeight(calc.finalFineWeight());
            line.setRatePerGram(calc.ratePerGram());
            line.setMetalAmount(calc.metalAmount());
            line.setMakingType(calc.makingType());
            line.setMakingRate(calc.makingRate());
            line.setMakingAmount(calc.makingAmount());
            line.setStoneAmount(calc.stoneAmount());
            line.setLineTotal(calc.lineTotal());
            invoice.getLines().add(line);

            metal = metal.add(calc.metalAmount());
            making = making.add(calc.makingAmount());
            stone = stone.add(calc.stoneAmount());
            taxable = taxable.add(calc.taxableAmount());
            tax = tax.add(calc.taxAmount());

            if (lineReq.stockItemId() != null && !lineReq.stockItemId().isBlank()) {
                stockService.postMovement(new InventoryDtos.PostMovementRequest(
                        lineReq.stockItemId(), request.branchId(), "IN", line.getQuantity(),
                        calc.grossWeight(), calc.netWeight(), calc.finalFineWeight(),
                        "PURCHASE", invoice.getId(), "Purchase " + invoice.getInvoiceNo()
                ), actor);
            }
        }
        invoice.setMetalAmount(MoneyMath.money(metal));
        invoice.setMakingAmount(MoneyMath.money(making));
        invoice.setStoneAmount(MoneyMath.money(stone));
        invoice.setTaxableAmount(MoneyMath.money(taxable));
        invoice.setTaxAmount(MoneyMath.money(tax));
        invoice.setGrandTotal(MoneyMath.money(taxable.add(tax)));
        repository.save(invoice);
        return toDto(invoice);
    }

    @GetMapping("/invoices")
    public List<PurchaseResponse> list() {
        return repository.findAllByOrderByCreatedAtDesc().stream().map(this::toDto).toList();
    }

    private PurchaseResponse toDto(PurchaseInvoice inv) {
        return new PurchaseResponse(
                inv.getId(), inv.getInvoiceNo(), inv.getInvoiceDate(), inv.getStatus(),
                inv.getBranchId(), inv.getSupplierId(), inv.getGrandTotal(),
                inv.getLines().stream().map(l -> new LineResponse(
                        l.getId(), l.getLineNo(), l.getItemName(), l.getNetWeight(),
                        l.getFinalFineWeight(), l.getMetalAmount(), l.getMakingAmount(), l.getLineTotal()
                )).toList()
        );
    }

    public record LineRequest(
            String stockItemId, @NotBlank String itemName, String metalId, String purityId,
            BigDecimal quantity, @NotNull BigDecimal grossWeight, BigDecimal stoneWeight, BigDecimal netWeight,
            @NotNull BigDecimal purityPercent, BigDecimal wastagePercent, @NotNull BigDecimal ratePerGram,
            String makingType, BigDecimal makingRate, BigDecimal stoneAmount
    ) {}

    public record CreatePurchaseRequest(
            @NotBlank String branchId, String supplierId, LocalDate invoiceDate, String remarks,
            @NotEmpty @Valid List<LineRequest> lines
    ) {}

    public record LineResponse(String id, int lineNo, String itemName, BigDecimal netWeight,
                               BigDecimal finalFineWeight, BigDecimal metalAmount, BigDecimal makingAmount, BigDecimal lineTotal) {}

    public record PurchaseResponse(String id, String invoiceNo, LocalDate invoiceDate, String status,
                                   String branchId, String supplierId, BigDecimal grandTotal, List<LineResponse> lines) {}
}
