package com.jewellery.erp.organization.api;

import com.jewellery.erp.organization.domain.Branch;
import com.jewellery.erp.organization.domain.BusinessFirm;
import com.jewellery.erp.organization.repository.BranchRepository;
import com.jewellery.erp.organization.repository.BusinessFirmRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/organization")
public class OrganizationController {

    private final BusinessFirmRepository firmRepository;
    private final BranchRepository branchRepository;

    public OrganizationController(BusinessFirmRepository firmRepository, BranchRepository branchRepository) {
        this.firmRepository = firmRepository;
        this.branchRepository = branchRepository;
    }

    @GetMapping("/firms")
    public List<FirmDto> firms() {
        return firmRepository.findAll().stream()
                .map(f -> new FirmDto(f.getId(), f.getCode(), f.getDisplayName(), f.getLegalName(), f.getCurrencyCode()))
                .toList();
    }

    @GetMapping("/branches")
    public List<BranchDto> branches() {
        return branchRepository.findAll().stream()
                .map(b -> new BranchDto(b.getId(), b.getFirmId(), b.getCode(), b.getName(), b.getStatus()))
                .toList();
    }

    public record FirmDto(String id, String code, String displayName, String legalName, String currencyCode) {
    }

    public record BranchDto(String id, String firmId, String code, String name, String status) {
    }
}
