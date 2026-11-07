package com.jewellery.erp.sales.api;

import com.jewellery.erp.sales.application.SalesService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sales")
public class SalesController {

    private final SalesService salesService;

    public SalesController(SalesService salesService) {
        this.salesService = salesService;
    }

    @PostMapping("/invoices")
    public SalesDtos.SaleResponse create(@Valid @RequestBody SalesDtos.CreateSaleRequest request, Authentication auth) {
        String actor = auth == null ? null : String.valueOf(auth.getPrincipal());
        return salesService.createAndFinalize(request, actor);
    }

    @GetMapping("/invoices")
    public List<SalesDtos.SaleResponse> list() {
        return salesService.list();
    }
}
