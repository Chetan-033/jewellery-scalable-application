package com.jewellery.erp.loans.application;

import com.jewellery.erp.loans.api.LoanDtos;
import com.jewellery.erp.loans.domain.GirviLoan;
import com.jewellery.erp.loans.repository.GirviLoanRepository;
import com.jewellery.erp.masterdata.repository.CustomerRepository;
import com.jewellery.erp.organization.application.DocumentNumberService;
import com.jewellery.erp.organization.repository.BranchRepository;
import com.jewellery.erp.shared.domain.BusinessException;
import com.jewellery.erp.shared.domain.MoneyMath;
import com.jewellery.erp.shared.domain.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
public class LoanService {

    private final GirviLoanRepository repository;
    private final CustomerRepository customerRepository;
    private final BranchRepository branchRepository;
    private final DocumentNumberService documentNumberService;

    public LoanService(
            GirviLoanRepository repository,
            CustomerRepository customerRepository,
            BranchRepository branchRepository,
            DocumentNumberService documentNumberService) {
        this.repository = repository;
        this.customerRepository = customerRepository;
        this.branchRepository = branchRepository;
        this.documentNumberService = documentNumberService;
    }

    @Transactional
    public LoanDtos.LoanResponse create(LoanDtos.CreateLoanRequest request, String actorUserId) {
        branchRepository.findById(request.branchId())
                .orElseThrow(() -> new NotFoundException("Branch not found"));
        customerRepository.findById(request.customerId())
                .orElseThrow(() -> new NotFoundException("Customer not found"));

        String interestType = request.interestType() == null || request.interestType().isBlank()
                ? "MONTHLY" : request.interestType().trim().toUpperCase();
        if (!List.of("MONTHLY", "YEARLY", "DAILY").contains(interestType)) {
            throw new BusinessException("INVALID_INTEREST_TYPE", "interestType must be MONTHLY, YEARLY or DAILY");
        }

        GirviLoan loan = new GirviLoan();
        loan.setId(UUID.randomUUID().toString());
        loan.setBranchId(request.branchId());
        loan.setCustomerId(request.customerId());
        loan.setTicketNo(documentNumberService.next("GIRVI"));
        loan.setOpenDate(request.openDate() == null ? LocalDate.now() : request.openDate());
        loan.setStatus(GirviLoan.ACTIVE);
        loan.setPrincipalAmount(MoneyMath.money(request.principalAmount()));
        loan.setRoiPercent(MoneyMath.purity(nz(request.roiPercent())));
        loan.setInterestType(interestType);
        loan.setMetalType(request.metalType() == null ? null : request.metalType().trim().toUpperCase());
        loan.setGrossWeight(MoneyMath.weight(nz(request.grossWeight())));
        loan.setNetWeight(MoneyMath.weight(nz(request.netWeight())));
        loan.setFineWeight(MoneyMath.weight(nz(request.fineWeight())));
        loan.setValuationAmount(MoneyMath.money(nz(request.valuationAmount())));
        loan.setItemDesc(request.itemDesc());
        loan.setRemarks(request.remarks());
        loan.setCreatedBy(actorUserId);
        applyInterest(loan, LocalDate.now());
        repository.save(loan);
        return toDto(loan);
    }

    /** status null/blank => all loans. */
    @Transactional(readOnly = true)
    public List<LoanDtos.LoanResponse> listActive(String status) {
        List<GirviLoan> rows = status == null || status.isBlank()
                ? repository.findAllByOrderByOpenDateDescCreatedAtDesc()
                : repository.findByStatusOrderByOpenDateDescCreatedAtDesc(status.trim().toUpperCase());
        return rows.stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<LoanDtos.LoanResponse> listByCustomer(String customerId) {
        return repository.findByCustomerIdOrderByOpenDateDescCreatedAtDesc(customerId)
                .stream().map(this::toDto).toList();
    }

    @Transactional
    public LoanDtos.LoanResponse release(String id) {
        GirviLoan loan = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Loan not found"));
        if (!GirviLoan.ACTIVE.equals(loan.getStatus())) {
            throw new BusinessException("LOAN_NOT_ACTIVE", "Only ACTIVE loans can be released");
        }
        applyInterest(loan, LocalDate.now());
        loan.setStatus(GirviLoan.RELEASED);
        loan.setUpdatedAt(Instant.now());
        repository.save(loan);
        return toDto(loan);
    }

    /** interest = principal * roi% * days / 30 for MONTHLY (YEARLY: /365, DAILY: per day). */
    static BigDecimal computeInterest(GirviLoan loan, LocalDate asOf) {
        long days = Math.max(0, ChronoUnit.DAYS.between(loan.getOpenDate(), asOf));
        BigDecimal base = loan.getPrincipalAmount()
                .multiply(loan.getRoiPercent())
                .divide(BigDecimal.valueOf(100), 8, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(days));
        BigDecimal divisor = switch (loan.getInterestType() == null ? "MONTHLY" : loan.getInterestType()) {
            case "YEARLY" -> BigDecimal.valueOf(365);
            case "DAILY" -> BigDecimal.ONE;
            default -> BigDecimal.valueOf(30);
        };
        return MoneyMath.money(base.divide(divisor, 8, RoundingMode.HALF_UP));
    }

    private void applyInterest(GirviLoan loan, LocalDate asOf) {
        BigDecimal interest = computeInterest(loan, asOf);
        loan.setInterestAmount(interest);
        loan.setTotalAmount(MoneyMath.money(loan.getPrincipalAmount().add(interest)));
    }

    private LoanDtos.LoanResponse toDto(GirviLoan loan) {
        LocalDate asOf = GirviLoan.ACTIVE.equals(loan.getStatus())
                ? LocalDate.now()
                : loan.getUpdatedAt().atZone(ZoneId.systemDefault()).toLocalDate();
        BigDecimal interest = loan.getInterestAmount();
        BigDecimal total = loan.getTotalAmount();
        if (GirviLoan.ACTIVE.equals(loan.getStatus())) {
            // live accrual for open loans (not persisted until release)
            interest = computeInterest(loan, asOf);
            total = MoneyMath.money(loan.getPrincipalAmount().add(interest));
        }
        long days = Math.max(0, ChronoUnit.DAYS.between(loan.getOpenDate(), asOf));
        return new LoanDtos.LoanResponse(
                loan.getId(), loan.getBranchId(), loan.getCustomerId(), loan.getTicketNo(), loan.getOpenDate(),
                loan.getStatus(), loan.getPrincipalAmount(), loan.getRoiPercent(), loan.getInterestType(),
                loan.getMetalType(), loan.getGrossWeight(), loan.getNetWeight(), loan.getFineWeight(),
                loan.getValuationAmount(), days, interest, total, loan.getItemDesc(), loan.getRemarks()
        );
    }

    private BigDecimal nz(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
