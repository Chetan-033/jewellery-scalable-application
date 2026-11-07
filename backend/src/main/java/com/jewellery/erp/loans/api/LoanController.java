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

    @GetMapping("/customer/{customerId}")
    public List<LoanDtos.LoanResponse> byCustomer(@PathVariable String customerId) {
        return loanService.listByCustomer(customerId);
    }

    @PostMapping("/{id}/release")
    public LoanDtos.LoanResponse release(@PathVariable String id) {
        return loanService.release(id);
    }
}
