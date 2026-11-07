package com.jewellery.erp.credit.api;

import com.jewellery.erp.credit.application.CreditService;
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
@RequestMapping("/api/v1/credit")
public class CreditController {

    private final CreditService creditService;

    public CreditController(CreditService creditService) {
        this.creditService = creditService;
    }

    @PostMapping("/udhaar")
    public CreditDtos.LedgerEntryResponse postUdhaar(
            @Valid @RequestBody CreditDtos.PostEntryRequest request, Authentication authentication) {
        return creditService.postUdhaar(request, actor(authentication));
    }

    @PostMapping("/advance")
    public CreditDtos.LedgerEntryResponse postAdvance(
            @Valid @RequestBody CreditDtos.PostEntryRequest request, Authentication authentication) {
        return creditService.postAdvance(request, actor(authentication));
    }

    @GetMapping("/udhaar/top")
    public List<CreditDtos.CustomerBalanceResponse> topUdhaar(@RequestParam(defaultValue = "5") int limit) {
        return creditService.listTopUdhaar(limit);
    }

    @GetMapping("/advance/top")
    public List<CreditDtos.CustomerBalanceResponse> topAdvance(@RequestParam(defaultValue = "5") int limit) {
        return creditService.listTopAdvance(limit);
    }

    @GetMapping("/customer/{customerId}")
    public CreditDtos.CustomerLedgerResponse byCustomer(@PathVariable String customerId) {
        return creditService.listByCustomer(customerId);
    }

    private String actor(Authentication authentication) {
        return authentication == null ? null : String.valueOf(authentication.getPrincipal());
    }
}
