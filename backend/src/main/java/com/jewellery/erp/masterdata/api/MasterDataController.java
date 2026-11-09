package com.jewellery.erp.masterdata.api;

import com.jewellery.erp.masterdata.application.MasterDataService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/masters")
public class MasterDataController {

    private final MasterDataService masterDataService;

    public MasterDataController(MasterDataService masterDataService) {
        this.masterDataService = masterDataService;
    }

    @PostMapping("/customers")
    public MasterDataDtos.CustomerResponse createCustomer(@Valid @RequestBody MasterDataDtos.CustomerRequest request) {
        return masterDataService.createCustomer(request);
    }

    @GetMapping("/customers/{id}")
    public MasterDataDtos.CustomerResponse getCustomer(@PathVariable String id) {
        return masterDataService.getCustomer(id);
    }

    @PutMapping("/customers/{id}")
    public MasterDataDtos.CustomerResponse updateCustomer(
            @PathVariable String id,
            @Valid @RequestBody MasterDataDtos.CustomerRequest request) {
        return masterDataService.updateCustomer(id, request);
    }

    @GetMapping("/customers")
    public List<MasterDataDtos.CustomerResponse> customers(@RequestParam(required = false) String q) {
        return masterDataService.listCustomers(q);
    }

    @PostMapping("/metals")
    public MasterDataDtos.MetalResponse createMetal(@Valid @RequestBody MasterDataDtos.MetalRequest request) {
        return masterDataService.createMetal(request);
    }

    @GetMapping("/metals")
    public List<MasterDataDtos.MetalResponse> metals() {
        return masterDataService.listMetals();
    }

    @PostMapping("/purities")
    public MasterDataDtos.PurityResponse createPurity(@Valid @RequestBody MasterDataDtos.PurityRequest request) {
        return masterDataService.createPurity(request);
    }

    @GetMapping("/purities")
    public List<MasterDataDtos.PurityResponse> purities() {
        return masterDataService.listPurities();
    }

    @PostMapping("/suppliers")
    public MasterDataDtos.SupplierResponse createSupplier(@Valid @RequestBody MasterDataDtos.SupplierRequest request) {
        return masterDataService.createSupplier(request);
    }

    @GetMapping("/suppliers")
    public List<MasterDataDtos.SupplierResponse> suppliers() {
        return masterDataService.listSuppliers();
    }
}
