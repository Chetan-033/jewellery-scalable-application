package com.jewellery.erp.sales.application;

import com.jewellery.erp.calculation.MetalLineInput;
import com.jewellery.erp.calculation.MetalLineResult;
import com.jewellery.erp.calculation.MetalValuationEngine;
import com.jewellery.erp.calculation.application.CalcSettingService;
import com.jewellery.erp.credit.api.CreditDtos;
import com.jewellery.erp.credit.application.CreditService;
import com.jewellery.erp.inventory.api.InventoryDtos;
import com.jewellery.erp.inventory.application.StockService;
import com.jewellery.erp.organization.application.DocumentNumberService;
import com.jewellery.erp.organization.repository.BranchRepository;
import com.jewellery.erp.sales.api.SalesDtos;
import com.jewellery.erp.sales.domain.SaleExchangeLine;
import com.jewellery.erp.sales.domain.SalesInvoice;
import com.jewellery.erp.sales.domain.SalesInvoiceLine;
import com.jewellery.erp.sales.repository.SaleExchangeLineRepository;
import com.jewellery.erp.sales.repository.SalesInvoiceRepository;
import com.jewellery.erp.shared.domain.BusinessException;
import com.jewellery.erp.shared.domain.MoneyMath;
import com.jewellery.erp.shared.domain.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SalesService {

    private final SalesInvoiceRepository salesInvoiceRepository;
    private final SaleExchangeLineRepository exchangeLineRepository;
    private final DocumentNumberService documentNumberService;
    private final MetalValuationEngine valuationEngine;
    private final BranchRepository branchRepository;
    private final StockService stockService;
    private final CreditService creditService;
    private final CalcSettingService calcSettingService;

    public SalesService(
            SalesInvoiceRepository salesInvoiceRepository,
            SaleExchangeLineRepository exchangeLineRepository,
            DocumentNumberService documentNumberService,
            MetalValuationEngine valuationEngine,
            BranchRepository branchRepository,
            StockService stockService,
            CreditService creditService,
            CalcSettingService calcSettingService) {
        this.salesInvoiceRepository = salesInvoiceRepository;
        this.exchangeLineRepository = exchangeLineRepository;
        this.documentNumberService = documentNumberService;
        this.valuationEngine = valuationEngine;
        this.branchRepository = branchRepository;
        this.stockService = stockService;
        this.creditService = creditService;
        this.calcSettingService = calcSettingService;
    }

    @Transactional
    public SalesDtos.SaleResponse createAndFinalize(SalesDtos.CreateSaleRequest request, String actorUserId) {
        branchRepository.findById(request.branchId())
                .orElseThrow(() -> new NotFoundException("Branch not found"));

        BigDecimal udhaar = MoneyMath.money(nz(request.udhaarAmount()));
        if (udhaar.signum() > 0 && (request.customerId() == null || request.customerId().isBlank())) {
            throw new BusinessException("CUSTOMER_REQUIRED", "customerId is required when udhaarAmount > 0");
        }

        SalesInvoice invoice = new SalesInvoice();
        invoice.setId(UUID.randomUUID().toString());
        invoice.setBranchId(request.branchId());
        invoice.setCustomerId(request.customerId());
        invoice.setInvoiceNo(documentNumberService.next("SALE"));
        invoice.setInvoiceDate(request.invoiceDate() == null ? LocalDate.now() : request.invoiceDate());
        invoice.setRemarks(request.remarks());
        invoice.setCreatedBy(actorUserId);
        applyPayments(invoice, request, udhaar);

        String custMode = calcSettingService.get("sellCustWstgMode", MetalValuationEngine.MODE_ADD_TO_NET_PCT);

        BigDecimal metalSum = BigDecimal.ZERO;
        BigDecimal makingSum = BigDecimal.ZERO;
        BigDecimal stoneSum = BigDecimal.ZERO;
        BigDecimal discountSum = BigDecimal.ZERO;
        BigDecimal taxableSum = BigDecimal.ZERO;
        BigDecimal cgstSum = BigDecimal.ZERO;
        BigDecimal sgstSum = BigDecimal.ZERO;
        BigDecimal igstSum = BigDecimal.ZERO;
        List<String> snapshots = new ArrayList<>();

        int lineNo = 1;
        for (SalesDtos.LineRequest lineReq : request.lines()) {
            MetalLineResult calc = valuationEngine.calculate(new MetalLineInput(
                    lineReq.quantity(),
                    lineReq.grossWeight(),
                    lineReq.stoneWeight(),
                    lineReq.netWeight(),
                    lineReq.purityPercent(),
                    lineReq.wastagePercent(),
                    lineReq.ratePerGram(),
                    lineReq.makingType(),
                    lineReq.makingRate(),
                    lineReq.stoneAmount(),
                    lineReq.discountAmount(),
                    lineReq.cgstPercent() == null ? BigDecimal.valueOf(1.5) : lineReq.cgstPercent(),
                    lineReq.sgstPercent() == null ? BigDecimal.valueOf(1.5) : lineReq.sgstPercent(),
                    lineReq.igstPercent(),
                    lineReq.custWastagePercent(),
                    custMode,
                    lineReq.valueAdded()
            ));

            SalesInvoiceLine line = new SalesInvoiceLine();
            line.setId(UUID.randomUUID().toString());
            line.setSalesInvoice(invoice);
            line.setLineNo(lineNo++);
            line.setStockItemId(lineReq.stockItemId());
            line.setItemName(lineReq.itemName());
            line.setMetalId(lineReq.metalId());
            line.setPurityId(lineReq.purityId());
            line.setQuantity(calc.netWeight().compareTo(BigDecimal.ZERO) >= 0
                    ? (lineReq.quantity() == null ? BigDecimal.ONE : lineReq.quantity()) : BigDecimal.ONE);
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
            line.setDiscountAmount(calc.discountAmount());
            line.setLineTotal(calc.lineTotal());
            line.setBarcode(lineReq.barcode());
            line.setCustWastagePercent(calc.custWastagePercent());
            line.setValueAdded(calc.valueAdded());
            invoice.getLines().add(line);

            metalSum = metalSum.add(calc.metalAmount());
            makingSum = makingSum.add(calc.makingAmount());
            stoneSum = stoneSum.add(calc.stoneAmount());
            discountSum = discountSum.add(calc.discountAmount());
            taxableSum = taxableSum.add(calc.taxableAmount());
            cgstSum = cgstSum.add(calc.cgstAmount());
            sgstSum = sgstSum.add(calc.sgstAmount());
            igstSum = igstSum.add(calc.igstAmount());
            snapshots.add(calc.formulaNotes());

            if (lineReq.stockItemId() != null && !lineReq.stockItemId().isBlank()) {
                stockService.postMovement(new InventoryDtos.PostMovementRequest(
                        lineReq.stockItemId(),
                        request.branchId(),
                        "OUT",
                        line.getQuantity(),
                        calc.grossWeight(),
                        calc.netWeight(),
                        calc.finalFineWeight(),
                        "SALE",
                        invoice.getId(),
                        "Sale " + invoice.getInvoiceNo()
                ), actorUserId);
            }
        }

        List<SaleExchangeLine> exchangeLines = buildExchangeLines(invoice.getId(), request.exchangeLines());
        BigDecimal exchangeTotal = exchangeLines.stream()
                .map(SaleExchangeLine::getValuation)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        invoice.setMetalAmount(MoneyMath.money(metalSum));
        invoice.setMakingAmount(MoneyMath.money(makingSum));
        invoice.setStoneAmount(MoneyMath.money(stoneSum));
        invoice.setDiscountAmount(MoneyMath.money(discountSum));
        invoice.setTaxableAmount(MoneyMath.money(taxableSum));
        invoice.setCgstAmount(MoneyMath.money(cgstSum));
        invoice.setSgstAmount(MoneyMath.money(sgstSum));
        invoice.setIgstAmount(MoneyMath.money(igstSum));
        invoice.setTaxAmount(MoneyMath.money(cgstSum.add(sgstSum).add(igstSum)));
        invoice.setHallmarkAmount(MoneyMath.money(nz(request.hallmarkAmount())));
        invoice.setOtherCharges(MoneyMath.money(nz(request.otherCharges())));
        invoice.setCourierCharges(MoneyMath.money(nz(request.courierCharges())));
        invoice.setExchangeAmount(MoneyMath.money(exchangeTotal));
        BigDecimal grand = taxableSum.add(invoice.getTaxAmount())
                .add(invoice.getHallmarkAmount())
                .add(invoice.getOtherCharges())
                .add(invoice.getCourierCharges())
                .subtract(invoice.getExchangeAmount());
        if (grand.signum() < 0) {
            throw new BusinessException("EXCHANGE_EXCEEDS_TOTAL", "Exchange value exceeds invoice total");
        }
        invoice.setGrandTotal(MoneyMath.money(grand));
        invoice.setStatus("FINALIZED");
        invoice.setCalcSnapshot(String.join(" | ", snapshots));
        salesInvoiceRepository.save(invoice);

        exchangeLineRepository.saveAll(exchangeLines);

        if (udhaar.signum() > 0) {
            creditService.postUdhaar(new CreditDtos.PostEntryRequest(
                    request.branchId(),
                    request.customerId(),
                    udhaar,
                    invoice.getInvoiceDate(),
                    "SALE",
                    invoice.getId(),
                    "Udhaar on sale " + invoice.getInvoiceNo()
            ), actorUserId);
        }
        return toDto(invoice, exchangeLines.stream().map(this::toExchangeDto).toList());
    }

    @Transactional(readOnly = true)
    public List<SalesDtos.SaleResponse> list() {
        List<SalesInvoice> invoices = salesInvoiceRepository.findAllByOrderByCreatedAtDesc();
        Map<String, List<SalesDtos.ExchangeLineResponse>> exchanges = loadExchangeLines(
                invoices.stream().map(SalesInvoice::getId).toList());
        return invoices.stream()
                .map(i -> toDto(i, exchanges.getOrDefault(i.getId(), List.of())))
                .toList();
    }

    /** Split payments; legacy paidAmount-only requests are treated as cash. paidAmount = cash+card+online+cheque. */
    private void applyPayments(SalesInvoice invoice, SalesDtos.CreateSaleRequest request, BigDecimal udhaar) {
        boolean split = request.cashAmount() != null || request.cardAmount() != null
                || request.onlineAmount() != null || request.chequeAmount() != null;
        BigDecimal cash = split ? nz(request.cashAmount()) : nz(request.paidAmount());
        BigDecimal card = split ? nz(request.cardAmount()) : BigDecimal.ZERO;
        BigDecimal online = split ? nz(request.onlineAmount()) : BigDecimal.ZERO;
        BigDecimal cheque = split ? nz(request.chequeAmount()) : BigDecimal.ZERO;
        invoice.setCashAmount(MoneyMath.money(cash));
        invoice.setCardAmount(MoneyMath.money(card));
        invoice.setOnlineAmount(MoneyMath.money(online));
        invoice.setChequeAmount(MoneyMath.money(cheque));
        invoice.setUdhaarAmount(udhaar);
        invoice.setPaidAmount(MoneyMath.money(cash.add(card).add(online).add(cheque)));
    }

    private List<SaleExchangeLine> buildExchangeLines(String invoiceId, List<SalesDtos.ExchangeLineRequest> requests) {
        List<SaleExchangeLine> result = new ArrayList<>();
        if (requests == null) {
            return result;
        }
        int no = 1;
        for (SalesDtos.ExchangeLineRequest r : requests) {
            BigDecimal gross = MoneyMath.weight(nz(r.grossWeight()));
            BigDecimal less = MoneyMath.weight(nz(r.lessWeight()));
            BigDecimal net = r.netWeight() != null ? MoneyMath.weight(r.netWeight()) : MoneyMath.weight(gross.subtract(less));
            if (net.signum() < 0) {
                throw new BusinessException("INVALID_EXCHANGE", "Exchange net weight cannot be negative");
            }
            BigDecimal purity = MoneyMath.purity(nz(r.purityPercent()));
            BigDecimal fine = MoneyMath.fineWeight(net, purity);
            BigDecimal rate = MoneyMath.rate(nz(r.ratePerGram()));

            SaleExchangeLine line = new SaleExchangeLine();
            line.setId(UUID.randomUUID().toString());
            line.setSalesInvoiceId(invoiceId);
            line.setLineNo(no++);
            line.setMetalType(r.metalType() == null || r.metalType().isBlank() ? "GOLD" : r.metalType().trim().toUpperCase());
            line.setDescription(r.description());
            line.setGrossWeight(gross);
            line.setLessWeight(less);
            line.setNetWeight(net);
            line.setPurityPercent(purity);
            line.setFineWeight(fine);
            line.setRatePerGram(rate);
            line.setValuation(MoneyMath.money(fine.multiply(rate)));
            result.add(line);
        }
        return result;
    }

    private Map<String, List<SalesDtos.ExchangeLineResponse>> loadExchangeLines(Collection<String> invoiceIds) {
        if (invoiceIds.isEmpty()) {
            return Map.of();
        }
        return exchangeLineRepository.findBySalesInvoiceIdInOrderByLineNoAsc(invoiceIds).stream()
                .collect(Collectors.groupingBy(SaleExchangeLine::getSalesInvoiceId,
                        Collectors.mapping(this::toExchangeDto, Collectors.toList())));
    }

    private BigDecimal nz(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private SalesDtos.ExchangeLineResponse toExchangeDto(SaleExchangeLine e) {
        return new SalesDtos.ExchangeLineResponse(
                e.getId(), e.getLineNo(), e.getMetalType(), e.getDescription(), e.getGrossWeight(),
                e.getLessWeight(), e.getNetWeight(), e.getPurityPercent(), e.getFineWeight(),
                e.getRatePerGram(), e.getValuation()
        );
    }

    private SalesDtos.SaleResponse toDto(SalesInvoice invoice, List<SalesDtos.ExchangeLineResponse> exchangeLines) {
        List<SalesDtos.LineResponse> lines = invoice.getLines().stream()
                .map(l -> new SalesDtos.LineResponse(
                        l.getId(), l.getLineNo(), l.getStockItemId(), l.getItemName(), l.getQuantity(),
                        l.getGrossWeight(), l.getNetWeight(), l.getPurityPercent(), l.getWastagePercent(),
                        l.getFineWeight(), l.getFinalFineWeight(), l.getRatePerGram(), l.getMetalAmount(),
                        l.getMakingType(), l.getMakingAmount(), l.getStoneAmount(), l.getLineTotal(),
                        l.getBarcode(), l.getCustWastagePercent(), l.getValueAdded()
                )).toList();
        return new SalesDtos.SaleResponse(
                invoice.getId(), invoice.getInvoiceNo(), invoice.getInvoiceDate(), invoice.getStatus(),
                invoice.getBranchId(), invoice.getCustomerId(), invoice.getMetalAmount(), invoice.getMakingAmount(),
                invoice.getStoneAmount(), invoice.getDiscountAmount(), invoice.getTaxableAmount(),
                invoice.getCgstAmount(), invoice.getSgstAmount(), invoice.getIgstAmount(), invoice.getTaxAmount(),
                invoice.getGrandTotal(), invoice.getPaidAmount(), lines,
                invoice.getCashAmount(), invoice.getCardAmount(), invoice.getOnlineAmount(), invoice.getChequeAmount(),
                invoice.getUdhaarAmount(), invoice.getExchangeAmount(), invoice.getHallmarkAmount(),
                invoice.getOtherCharges(), invoice.getCourierCharges(), exchangeLines
        );
    }
}
