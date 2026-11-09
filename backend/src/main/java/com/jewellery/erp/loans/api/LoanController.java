package com.jewellery.erp.loans.api;

import com.jewellery.erp.loans.application.LoanService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/loans")
public class LoanController {

    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    @PostMapping
    public LoanDtos.LoanResponse create(@Valid @RequestBody LoanDtos.CreateLoanRequest request, Authentication auth) {
        String actor = auth == null ? null : String.valueOf(auth.getPrincipal());
        return loanService.create(request, actor);
    }

    @GetMapping
    public List<LoanDtos.LoanResponse> list(@RequestParam(defaultValue = "ACTIVE") String status) {
        return loanService.listActive(status);
    }

    @GetMapping("/{id}")
    public LoanDtos.LoanResponse getById(@PathVariable String id) {
        return loanService.getById(id);
    }

    @GetMapping("/customer/{customerId}")
    public List<LoanDtos.LoanResponse> byCustomer(@PathVariable String customerId) {
        return loanService.listByCustomer(customerId);
    }

    @PostMapping("/{id}/release")
    public LoanDtos.LoanResponse release(
            @PathVariable String id,
            @RequestBody(required = false) LoanDtos.ReleaseLoanRequest request
    ) {
        return loanService.release(id, request);
    }

    @GetMapping("/{id}/settlement-preview")
    public LoanDtos.SettlementPreviewResponse getSettlementPreview(
            @PathVariable String id,
            @RequestParam(required = false) java.time.LocalDate releaseDate,
            @RequestParam(required = false, defaultValue = "DD") String option,
            @RequestParam(required = false) java.math.BigDecimal discount,
            @RequestParam(required = false) java.math.BigDecimal extra
    ) {
        return loanService.getSettlementPreview(id, releaseDate, option, discount, extra);
    }

    @PostMapping("/{id}/status")
    public LoanDtos.LoanResponse updateStatus(@PathVariable String id, @RequestParam String status) {
        return loanService.updateStatus(id, status);
    }
}
