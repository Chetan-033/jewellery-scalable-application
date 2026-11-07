package com.jewellery.erp.credit.application;

import com.jewellery.erp.credit.api.CreditDtos;
import com.jewellery.erp.credit.domain.CustomerBalance;
import com.jewellery.erp.credit.domain.PartyLedgerEntry;
import com.jewellery.erp.credit.repository.PartyLedgerEntryRepository;
import com.jewellery.erp.masterdata.domain.Customer;
import com.jewellery.erp.masterdata.repository.CustomerRepository;
import com.jewellery.erp.organization.application.DocumentNumberService;
import com.jewellery.erp.organization.repository.BranchRepository;
import com.jewellery.erp.shared.domain.MoneyMath;
import com.jewellery.erp.shared.domain.NotFoundException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CreditService {

    private static final int MAX_TOP = 100;

    private final PartyLedgerEntryRepository repository;
    private final CustomerRepository customerRepository;
    private final BranchRepository branchRepository;
    private final DocumentNumberService documentNumberService;

    public CreditService(
            PartyLedgerEntryRepository repository,
            CustomerRepository customerRepository,
            BranchRepository branchRepository,
            DocumentNumberService documentNumberService) {
        this.repository = repository;
        this.customerRepository = customerRepository;
        this.branchRepository = branchRepository;
        this.documentNumberService = documentNumberService;
    }

    @Transactional
    public CreditDtos.LedgerEntryResponse postUdhaar(CreditDtos.PostEntryRequest request, String actorUserId) {
        return toDto(post(PartyLedgerEntry.UDHAAR, "UDHAAR", request, actorUserId));
    }

    @Transactional
    public CreditDtos.LedgerEntryResponse postAdvance(CreditDtos.PostEntryRequest request, String actorUserId) {
        return toDto(post(PartyLedgerEntry.ADVANCE, "ADVANCE", request, actorUserId));
    }

    @Transactional(readOnly = true)
    public List<CreditDtos.CustomerBalanceResponse> listTopUdhaar(int limit) {
        return withCustomers(repository.topUdhaar(PageRequest.of(0, clamp(limit))));
    }

    @Transactional(readOnly = true)
    public List<CreditDtos.CustomerBalanceResponse> listTopAdvance(int limit) {
        return withCustomers(repository.topAdvance(PageRequest.of(0, clamp(limit))));
    }

    @Transactional(readOnly = true)
    public CreditDtos.CustomerLedgerResponse listByCustomer(String customerId) {
        customerRepository.findById(customerId)
                .orElseThrow(() -> new NotFoundException("Customer not found"));
        List<CreditDtos.LedgerEntryResponse> entries = repository
                .findByCustomerIdOrderByEntryDateDescCreatedAtDesc(customerId)
                .stream().map(this::toDto).toList();
        return new CreditDtos.CustomerLedgerResponse(
                customerId,
                MoneyMath.money(repository.udhaarBalance(customerId)),
                MoneyMath.money(repository.advanceBalance(customerId)),
                entries
        );
    }

    private PartyLedgerEntry post(String type, String docType, CreditDtos.PostEntryRequest request, String actor) {
        branchRepository.findById(request.branchId())
                .orElseThrow(() -> new NotFoundException("Branch not found"));
        customerRepository.findById(request.customerId())
                .orElseThrow(() -> new NotFoundException("Customer not found"));

        BigDecimal amount = MoneyMath.money(request.amount());
        PartyLedgerEntry entry = new PartyLedgerEntry();
        entry.setId(UUID.randomUUID().toString());
        entry.setBranchId(request.branchId());
        entry.setCustomerId(request.customerId());
        entry.setEntryType(type);
        entry.setRefType(request.refType());
        entry.setRefId(request.refId());
        entry.setRefNo(documentNumberService.next(docType));
        entry.setEntryDate(request.entryDate() == null ? LocalDate.now() : request.entryDate());
        entry.setAmount(amount);
        BigDecimal previous = PartyLedgerEntry.UDHAAR.equals(type)
                ? repository.udhaarBalance(request.customerId())
                : repository.advanceBalance(request.customerId());
        entry.setBalanceAfter(MoneyMath.money(previous.add(amount)));
        entry.setRemarks(request.remarks());
        entry.setCreatedBy(actor);
        return repository.save(entry);
    }

    private List<CreditDtos.CustomerBalanceResponse> withCustomers(List<CustomerBalance> rows) {
        Map<String, Customer> customers = customerRepository
                .findAllById(rows.stream().map(CustomerBalance::customerId).toList())
                .stream().collect(Collectors.toMap(Customer::getId, Function.identity()));
        return rows.stream().map(r -> {
            Customer c = customers.get(r.customerId());
            return new CreditDtos.CustomerBalanceResponse(
                    r.customerId(),
                    c == null ? null : c.getCode(),
                    c == null ? null : c.getName(),
                    c == null ? null : c.getPhone(),
                    MoneyMath.money(r.balance())
            );
        }).toList();
    }

    private int clamp(int limit) {
        return Math.max(1, Math.min(limit, MAX_TOP));
    }

    private CreditDtos.LedgerEntryResponse toDto(PartyLedgerEntry e) {
        return new CreditDtos.LedgerEntryResponse(
                e.getId(), e.getBranchId(), e.getCustomerId(), e.getEntryType(), e.getRefType(), e.getRefId(),
                e.getRefNo(), e.getEntryDate(), e.getAmount(), e.getBalanceAfter(), e.getRemarks(), e.getCreatedAt()
        );
    }
}
