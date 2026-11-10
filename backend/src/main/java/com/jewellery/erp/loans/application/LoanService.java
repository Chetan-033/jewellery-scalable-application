package com.jewellery.erp.loans.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jewellery.erp.loans.api.LoanDtos;
import com.jewellery.erp.loans.domain.GirviLoan;
import com.jewellery.erp.loans.domain.GirviLoanItem;
import com.jewellery.erp.loans.repository.GirviLoanItemRepository;
import com.jewellery.erp.loans.repository.GirviLoanRepository;
import com.jewellery.erp.masterdata.repository.CustomerRepository;
import com.jewellery.erp.organization.application.DocumentNumberService;
import com.jewellery.erp.organization.repository.BranchRepository;
import com.jewellery.erp.shared.domain.BusinessException;
import com.jewellery.erp.shared.domain.MoneyMath;
import com.jewellery.erp.shared.domain.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class LoanService {

    private static final Logger log = LoggerFactory.getLogger(LoanService.class);

    private final GirviLoanRepository repository;
    private final GirviLoanItemRepository itemRepository;
    private final CustomerRepository customerRepository;
    private final BranchRepository branchRepository;
    private final DocumentNumberService documentNumberService;
    private final ObjectMapper objectMapper;
    private final GirviDocumentStorageService documentStorageService;

    public LoanService(
            GirviLoanRepository repository,
            GirviLoanItemRepository itemRepository,
            CustomerRepository customerRepository,
            BranchRepository branchRepository,
            DocumentNumberService documentNumberService,
            ObjectMapper objectMapper,
            GirviDocumentStorageService documentStorageService) {
        this.repository = repository;
        this.itemRepository = itemRepository;
        this.customerRepository = customerRepository;
        this.branchRepository = branchRepository;
        this.documentNumberService = documentNumberService;
        this.objectMapper = objectMapper;
        this.documentStorageService = documentStorageService;
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
        loan.setMetalType(request.metalType() == null ? "GOLD" : request.metalType().trim().toUpperCase());

        // Extended fields
        loan.setPacketNo(request.packetNo());
        loan.setBoxNo(request.boxNo());
        loan.setLoanType(request.loanType() == null ? "SECURED" : request.loanType());
        loan.setFirstMonthInterest(Boolean.TRUE.equals(request.firstMonthInterest()));
        loan.setTimePeriodMonths(request.timePeriodMonths() == null ? 12 : request.timePeriodMonths());
        loan.setDrAccount(request.drAccount() == null ? "Secured Loans" : request.drAccount());
        loan.setItemLocation(request.itemLocation());
        loan.setCashAmount(MoneyMath.money(nz(request.cashAmount())));
        loan.setCashNarration(request.cashNarration());
        loan.setBankAmount(MoneyMath.money(nz(request.bankAmount())));
        loan.setBankNarration(request.bankNarration());
        loan.setCardAmount(MoneyMath.money(nz(request.cardAmount())));
        loan.setCardNarration(request.cardNarration());
        loan.setOnlineAmount(MoneyMath.money(nz(request.onlineAmount())));
        loan.setOnlineNarration(request.onlineNarration());

        // Process pledged items
        List<GirviLoanItem> itemEntities = new ArrayList<>();
        BigDecimal totalGross = BigDecimal.ZERO;
        BigDecimal totalNet = BigDecimal.ZERO;
        BigDecimal totalFine = BigDecimal.ZERO;
        BigDecimal totalValuation = BigDecimal.ZERO;
        List<String> itemNames = new ArrayList<>();

        if (request.items() != null && !request.items().isEmpty()) {
            int lineNo = 1;
            for (LoanDtos.PledgedItemDto itemDto : request.items()) {
                GirviLoanItem item = new GirviLoanItem();
                item.setId(UUID.randomUUID().toString());
                item.setGirviLoanId(loan.getId());
                item.setLineNo(lineNo++);
                item.setMetalType(itemDto.metalType() == null ? "GOLD" : itemDto.metalType().trim().toUpperCase());
                item.setItemName(itemDto.itemName() == null ? "Pledged Item" : itemDto.itemName().trim());
                item.setQuantity(itemDto.quantity() == null ? 1 : itemDto.quantity());
                item.setGrossWeight(MoneyMath.weight(nz(itemDto.grossWeight())));
                item.setNetWeight(MoneyMath.weight(nz(itemDto.netWeight())));
                item.setLessWeight(MoneyMath.weight(nz(itemDto.lessWeight())));
                item.setPurityPercent(MoneyMath.purity(nz(itemDto.purityPercent())));
                item.setFineWeight(MoneyMath.weight(nz(itemDto.fineWeight())));
                item.setValuationAmount(MoneyMath.money(nz(itemDto.valuationAmount())));
                String savedPhotoUrl = documentStorageService.saveIfBase64("pledge-" + item.getId(), itemDto.photoUrl());
                item.setPhotoUrl(savedPhotoUrl);

                itemEntities.add(item);
                totalGross = totalGross.add(item.getGrossWeight());
                totalNet = totalNet.add(item.getNetWeight());
                totalFine = totalFine.add(item.getFineWeight());
                totalValuation = totalValuation.add(item.getValuationAmount());
                itemNames.add(item.getItemName() + " (" + item.getQuantity() + ")");
            }
        }

        // Apply aggregated or request weights
        loan.setGrossWeight(totalGross.compareTo(BigDecimal.ZERO) > 0 ? totalGross : MoneyMath.weight(nz(request.grossWeight())));
        loan.setNetWeight(totalNet.compareTo(BigDecimal.ZERO) > 0 ? totalNet : MoneyMath.weight(nz(request.netWeight())));
        loan.setFineWeight(totalFine.compareTo(BigDecimal.ZERO) > 0 ? totalFine : MoneyMath.weight(nz(request.fineWeight())));
        loan.setValuationAmount(totalValuation.compareTo(BigDecimal.ZERO) > 0 ? totalValuation : MoneyMath.money(nz(request.valuationAmount())));

        if (request.itemDesc() != null && !request.itemDesc().isBlank()) {
            loan.setItemDesc(request.itemDesc());
        } else if (!itemNames.isEmpty()) {
            loan.setItemDesc(String.join(", ", itemNames));
        }

        loan.setRemarks(request.remarks());
        loan.setCreatedBy(actorUserId);

        try {
            if (request.items() != null && !request.items().isEmpty()) {
                loan.setItemsJson(objectMapper.writeValueAsString(request.items()));
            }
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize loan items to JSON", e);
        }

        applyInterest(loan, LocalDate.now());
        repository.save(loan);

        if (!itemEntities.isEmpty()) {
            itemRepository.saveAll(itemEntities);
        }

        return toDto(loan);
    }

    @Transactional(readOnly = true)
    public List<LoanDtos.LoanResponse> listActive(String status) {
        List<GirviLoan> rows = (status == null || status.isBlank() || "ALL".equalsIgnoreCase(status.trim()))
                ? repository.findAllByOrderByOpenDateDescCreatedAtDesc()
                : repository.findByStatusOrderByOpenDateDescCreatedAtDesc(status.trim().toUpperCase());
        return rows.stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<LoanDtos.LoanResponse> listByCustomer(String customerId) {
        return repository.findByCustomerIdOrderByOpenDateDescCreatedAtDesc(customerId)
                .stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public LoanDtos.LoanResponse getById(String id) {
        GirviLoan loan = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Loan not found: " + id));
        return toDto(loan);
    }

    @Transactional
    public LoanDtos.LoanResponse release(String id, LoanDtos.ReleaseLoanRequest req) {
        GirviLoan loan = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Loan not found: " + id));
        if (!GirviLoan.ACTIVE.equals(loan.getStatus())) {
            throw new BusinessException("LOAN_NOT_ACTIVE", "Only ACTIVE loans can be released");
        }

        LocalDate relDate = (req != null && req.releaseDate() != null) ? req.releaseDate() : LocalDate.now();
        String option = (req != null && req.monthlyInterestOption() != null) ? req.monthlyInterestOption().toUpperCase() : "DD";

        BigDecimal interest = (req != null && req.customInterestAmount() != null && req.customInterestAmount().compareTo(BigDecimal.ZERO) >= 0)
                ? req.customInterestAmount()
                : computeInterestWithOptions(loan, relDate, option);

        BigDecimal discount = (req != null && req.discountAmount() != null) ? req.discountAmount() : BigDecimal.ZERO;
        BigDecimal extra = (req != null && req.extraAmount() != null) ? req.extraAmount() : BigDecimal.ZERO;

        BigDecimal totalDue = MoneyMath.money(loan.getPrincipalAmount().add(interest).add(extra).subtract(discount));
        if (totalDue.compareTo(BigDecimal.ZERO) < 0) {
            totalDue = BigDecimal.ZERO;
        }

        loan.setStatus(GirviLoan.RELEASED);
        loan.setReleaseDate(relDate);
        loan.setReleaseTicketNo("REL-" + loan.getTicketNo());
        loan.setPaidInterest(interest);
        loan.setDiscountAmount(discount);
        loan.setExtraAmount(extra);
        loan.setPaidAmount(totalDue);
        loan.setMonthlyInterestOption(option);
        loan.setInterestAmount(interest);
        loan.setTotalAmount(totalDue);
        loan.setLockerStatus("OPEN");

        if (req != null) {
            loan.setReleaseCashAmount(req.cashAmount() != null ? req.cashAmount() : BigDecimal.ZERO);
            loan.setReleaseBankAmount(req.bankAmount() != null ? req.bankAmount() : BigDecimal.ZERO);
            loan.setReleaseCardAmount(req.cardAmount() != null ? req.cardAmount() : BigDecimal.ZERO);
            loan.setReleaseOnlineAmount(req.onlineAmount() != null ? req.onlineAmount() : BigDecimal.ZERO);
            if (req.remarks() != null && !req.remarks().isBlank()) {
                loan.setReleaseRemarks(req.remarks());
            }
        }

        loan.setUpdatedAt(Instant.now());
        repository.save(loan);
        return toDto(loan);
    }

    public LoanDtos.SettlementPreviewResponse getSettlementPreview(
            String id,
            LocalDate releaseDate,
            String option,
            BigDecimal discount,
            BigDecimal extra
    ) {
        GirviLoan loan = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Loan not found: " + id));

        LocalDate relDate = releaseDate != null ? releaseDate : LocalDate.now();
        String opt = option != null && !option.isBlank() ? option.toUpperCase() : "DD";
        BigDecimal disc = discount != null ? discount : BigDecimal.ZERO;
        BigDecimal ext = extra != null ? extra : BigDecimal.ZERO;

        long daysElapsed = Math.max(0, ChronoUnit.DAYS.between(loan.getOpenDate(), relDate));
        java.time.Period period = java.time.Period.between(loan.getOpenDate(), relDate);
        long months = Math.max(0, period.getYears() * 12L + period.getMonths());
        long days = Math.max(0, period.getDays());

        BigDecimal interest = computeInterestWithOptions(loan, relDate, opt);
        BigDecimal totalDue = MoneyMath.money(loan.getPrincipalAmount().add(interest).add(ext).subtract(disc));
        if (totalDue.compareTo(BigDecimal.ZERO) < 0) totalDue = BigDecimal.ZERO;

        return new LoanDtos.SettlementPreviewResponse(
                loan.getId(),
                loan.getTicketNo(),
                loan.getOpenDate(),
                relDate,
                daysElapsed,
                months,
                days,
                opt,
                loan.getPrincipalAmount(),
                loan.getRoiPercent(),
                loan.getInterestType(),
                interest,
                disc,
                ext,
                totalDue
        );
    }

    /**
     * Complete Omunim Core PHP Interest Calculation (ommpgvip.php)
     * Options:
     * - DD: Default exact days elapsed (Principal * ROI% * days / 3000)
     * - FM: Full Month - fractional days rounded up to next full month
     * - HM: Half Month - <= 15 days rounded to 15 days, > 15 days rounded to full month
     * - 7D: 7 Days slab - 7, 14, 21, or 30 days
     */
    public static BigDecimal computeInterestWithOptions(GirviLoan loan, LocalDate asOf, String option) {
        if (asOf == null || asOf.isBefore(loan.getOpenDate())) {
            asOf = loan.getOpenDate();
        }

        long totalElapsedDays = Math.max(0, ChronoUnit.DAYS.between(loan.getOpenDate(), asOf));
        BigDecimal principal = loan.getPrincipalAmount() != null ? loan.getPrincipalAmount() : BigDecimal.ZERO;
        BigDecimal roi = loan.getRoiPercent() != null ? loan.getRoiPercent() : BigDecimal.ZERO;

        // Base monthly rate = (Principal * ROI) / 100
        BigDecimal perMonthInt = principal.multiply(roi).divide(BigDecimal.valueOf(100), 8, RoundingMode.HALF_UP);
        BigDecimal perDayInt = perMonthInt.divide(BigDecimal.valueOf(30), 8, RoundingMode.HALF_UP);

        java.time.Period period = java.time.Period.between(loan.getOpenDate(), asOf);
        int totalYears = Math.max(0, period.getYears());
        int totalMonths = Math.max(0, period.getMonths());
        int totalDays = Math.max(0, period.getDays());

        // Omunim firstMonthInterest rule: if loan has first_month_interest set, minimum 1 month charged if < 30 days
        if (Boolean.TRUE.equals(loan.getFirstMonthInterest())) {
            if (totalYears == 0 && totalMonths == 0 && totalDays < 30) {
                totalMonths = 1;
                totalDays = 0;
            }
        }

        String opt = option != null ? option.toUpperCase().trim() : "DD";
        BigDecimal finalInterest;

        switch (opt) {
            case "FM" -> {
                // Full Month: any days in the current month charges an entire month
                int monthsCharged = totalYears * 12 + totalMonths;
                if (totalDays > 0) {
                    monthsCharged += 1;
                }
                finalInterest = perMonthInt.multiply(BigDecimal.valueOf(monthsCharged));
            }
            case "HM" -> {
                // Half Month: <= 15 days charges 15 days, > 15 days charges 30 days
                int monthsCharged = totalYears * 12 + totalMonths;
                int daysCharged = 0;
                if (totalDays > 15) {
                    monthsCharged += 1;
                } else if (totalDays > 0) {
                    daysCharged = 15;
                }
                finalInterest = perMonthInt.multiply(BigDecimal.valueOf(monthsCharged))
                        .add(perDayInt.multiply(BigDecimal.valueOf(daysCharged)));
            }
            case "7D" -> {
                // 7 Days slab: 7, 14, 21, 30
                int monthsCharged = totalYears * 12 + totalMonths;
                int daysCharged = 0;
                if (totalDays > 21) {
                    monthsCharged += 1;
                } else if (totalDays > 14) {
                    daysCharged = 21;
                } else if (totalDays > 7) {
                    daysCharged = 14;
                } else if (totalDays > 0) {
                    daysCharged = 7;
                }
                finalInterest = perMonthInt.multiply(BigDecimal.valueOf(monthsCharged))
                        .add(perDayInt.multiply(BigDecimal.valueOf(daysCharged)));
            }
            default -> {
                // "DD" or Default exact elapsed days
                long daysToUse = totalElapsedDays;
                if (Boolean.TRUE.equals(loan.getFirstMonthInterest()) && daysToUse < 30) {
                    daysToUse = 30;
                }
                finalInterest = perDayInt.multiply(BigDecimal.valueOf(daysToUse));
            }
        }

        return MoneyMath.money(finalInterest);
    }

    @Transactional
    public LoanDtos.LoanResponse updateStatus(String id, String newStatus) {
        GirviLoan loan = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Loan not found: " + id));
        loan.setStatus(newStatus == null ? GirviLoan.ACTIVE : newStatus.trim().toUpperCase());
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
            interest = computeInterest(loan, asOf);
            total = MoneyMath.money(loan.getPrincipalAmount().add(interest));
        }
        long days = Math.max(0, ChronoUnit.DAYS.between(loan.getOpenDate(), asOf));

        // Load items
        List<GirviLoanItem> itemEntities = itemRepository.findByGirviLoanIdOrderByLineNoAsc(loan.getId());
        List<LoanDtos.PledgedItemDto> itemDtos = new ArrayList<>();
        for (GirviLoanItem item : itemEntities) {
            itemDtos.add(new LoanDtos.PledgedItemDto(
                    item.getId(),
                    item.getMetalType(),
                    item.getItemName(),
                    item.getQuantity(),
                    item.getGrossWeight(),
                    item.getNetWeight(),
                    item.getLessWeight(),
                    item.getPurityPercent(),
                    item.getFineWeight(),
                    item.getValuationAmount(),
                    item.getPhotoUrl()
            ));
        }

        // Customer details
        String customerName = "Customer";
        String customerCode = "";
        String customerPhone = "";
        String customerCity = "";
        var custOpt = customerRepository.findById(loan.getCustomerId());
        if (custOpt.isPresent()) {
            var c = custOpt.get();
            customerName = c.getName();
            customerCode = c.getCode();
            customerPhone = c.getPhone() == null ? "" : c.getPhone();
            customerCity = c.getCity() == null ? "" : c.getCity();
        }

        return new LoanDtos.LoanResponse(
                loan.getId(), loan.getBranchId(), loan.getCustomerId(),
                customerName, customerCode, customerPhone, customerCity,
                loan.getTicketNo(), loan.getOpenDate(),
                loan.getStatus(), loan.getPrincipalAmount(), loan.getRoiPercent(), loan.getInterestType(),
                loan.getMetalType(), loan.getGrossWeight(), loan.getNetWeight(), loan.getFineWeight(),
                loan.getValuationAmount(), days, interest, total, loan.getItemDesc(), loan.getRemarks(),
                loan.getPacketNo(), loan.getBoxNo(), loan.getLoanType(), loan.getFirstMonthInterest(),
                loan.getTimePeriodMonths(), loan.getDrAccount(), loan.getItemLocation(),
                loan.getCashAmount(), loan.getCashNarration(), loan.getBankAmount(), loan.getBankNarration(),
                loan.getCardAmount(), loan.getCardNarration(), loan.getOnlineAmount(), loan.getOnlineNarration(),
                itemDtos,
                loan.getReleaseDate(),
                loan.getReleaseTicketNo(),
                loan.getDiscountAmount() != null ? loan.getDiscountAmount() : BigDecimal.ZERO,
                loan.getExtraAmount() != null ? loan.getExtraAmount() : BigDecimal.ZERO,
                loan.getPaidAmount() != null ? loan.getPaidAmount() : total,
                loan.getPaidInterest() != null ? loan.getPaidInterest() : interest,
                loan.getMonthlyInterestOption() != null ? loan.getMonthlyInterestOption() : "DD",
                loan.getReleaseCashAmount() != null ? loan.getReleaseCashAmount() : BigDecimal.ZERO,
                loan.getReleaseBankAmount() != null ? loan.getReleaseBankAmount() : BigDecimal.ZERO,
                loan.getReleaseCardAmount() != null ? loan.getReleaseCardAmount() : BigDecimal.ZERO,
                loan.getReleaseOnlineAmount() != null ? loan.getReleaseOnlineAmount() : BigDecimal.ZERO,
                loan.getReleaseRemarks(),
                loan.getLockerStatus() != null ? loan.getLockerStatus() : "LOCKED"
        );
    }

    private BigDecimal nz(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
