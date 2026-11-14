package com.jewellery.erp.masterdata.application;

import com.jewellery.erp.masterdata.api.MasterDataDtos;
import com.jewellery.erp.masterdata.domain.Customer;
import com.jewellery.erp.masterdata.domain.Metal;
import com.jewellery.erp.masterdata.domain.Purity;
import com.jewellery.erp.masterdata.domain.Supplier;
import com.jewellery.erp.masterdata.repository.CustomerRepository;
import com.jewellery.erp.masterdata.repository.MetalRepository;
import com.jewellery.erp.masterdata.repository.PurityRepository;
import com.jewellery.erp.masterdata.repository.SupplierRepository;
import com.jewellery.erp.organization.application.DocumentNumberService;
import com.jewellery.erp.shared.api.PageRequests;
import com.jewellery.erp.shared.api.PageResponse;
import com.jewellery.erp.shared.domain.BusinessException;
import com.jewellery.erp.shared.domain.NotFoundException;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class MasterDataService {

    private final CustomerRepository customerRepository;
    private final MetalRepository metalRepository;
    private final PurityRepository purityRepository;
    private final SupplierRepository supplierRepository;
    private final DocumentNumberService documentNumberService;
    private final CustomerDocumentStorageService storageService;

    public MasterDataService(
            CustomerRepository customerRepository,
            MetalRepository metalRepository,
            PurityRepository purityRepository,
            SupplierRepository supplierRepository,
            DocumentNumberService documentNumberService,
            CustomerDocumentStorageService storageService) {
        this.customerRepository = customerRepository;
        this.metalRepository = metalRepository;
        this.purityRepository = purityRepository;
        this.supplierRepository = supplierRepository;
        this.documentNumberService = documentNumberService;
        this.storageService = storageService;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void migrateLegacyBase64Images() {
        List<Customer> customers = customerRepository.findAll();
        for (Customer c : customers) {
            boolean updated = false;
            if (c.getPhotoBase64() != null && c.getPhotoBase64().startsWith("data:")) {
                c.setPhotoBase64(storageService.saveIfBase64(c.getId() + "_photo", c.getPhotoBase64()));
                updated = true;
            }
            if (c.getAadhaarFrontBase64() != null && c.getAadhaarFrontBase64().startsWith("data:")) {
                c.setAadhaarFrontBase64(storageService.saveIfBase64(c.getId() + "_aadhaar_front", c.getAadhaarFrontBase64()));
                updated = true;
            }
            if (c.getAadhaarBackBase64() != null && c.getAadhaarBackBase64().startsWith("data:")) {
                c.setAadhaarBackBase64(storageService.saveIfBase64(c.getId() + "_aadhaar_back", c.getAadhaarBackBase64()));
                updated = true;
            }
            if (updated) {
                customerRepository.save(c);
            }
        }
    }

    @Transactional
    public MasterDataDtos.CustomerResponse createCustomer(MasterDataDtos.CustomerRequest request) {
        String code = trim(request.code());
        if (code == null || code.isBlank()) {
            code = documentNumberService.next("CUSTOMER");
        } else if (customerRepository.existsByCode(code)) {
            throw new BusinessException("CUSTOMER_CODE_EXISTS", "Customer code already exists: " + code);
        }

        Customer customer = new Customer();
        customer.setId(UUID.randomUUID().toString());
        customer.setCode(code);
        applyCustomerFields(customer, request);
        customerRepository.save(customer);
        return toCustomer(customer);
    }

    @Transactional
    public MasterDataDtos.CustomerResponse updateCustomer(String id, MasterDataDtos.CustomerRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Customer not found: " + id));
        applyCustomerFields(customer, request);
        customerRepository.save(customer);
        return toCustomer(customer);
    }

    @Transactional(readOnly = true)
    public MasterDataDtos.CustomerResponse getCustomer(String id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Customer not found: " + id));
        return toCustomer(customer);
    }

    @Transactional(readOnly = true)
    public PageResponse<MasterDataDtos.CustomerSummary> listCustomers(String q, Integer page, Integer size) {
        String query = q == null ? "" : q.trim();
        Page<Customer> rows = customerRepository.searchPage(
                query,
                PageRequests.of(page, size, Sort.by(Sort.Direction.ASC, "name"))
        );
        return PageResponse.from(rows.map(this::toCustomerSummary));
    }

    private void applyCustomerFields(Customer customer, MasterDataDtos.CustomerRequest request) {
        String first = trim(request.firstName());
        String last = trim(request.lastName());
        String name = trim(request.name());
        if (name == null || name.isBlank()) {
            name = ((first == null ? "" : first) + " " + (last == null ? "" : last)).trim();
        }
        if (name == null || name.isBlank()) {
            throw new BusinessException("CUSTOMER_NAME_REQUIRED", "Customer name or first/last name is required");
        }
        customer.setName(name);
        customer.setFirstName(first);
        customer.setLastName(last);
        customer.setFatherName(trim(request.fatherName()));
        customer.setPhone(trim(request.phone()));
        customer.setEmail(trim(request.email()));
        customer.setGstin(trim(request.gstin()));
        customer.setCity(trim(request.city()));
        customer.setAddress(trim(request.address()));
        customer.setTitle(trim(request.title()));
        customer.setGender(trim(request.gender()));
        customer.setSpouseName(trim(request.spouseName()));
        customer.setAltPhone(trim(request.altPhone()));
        customer.setStateName(trim(request.stateName()));
        customer.setVillage(trim(request.village()));
        customer.setPincode(trim(request.pincode()));
        customer.setPanNo(trim(request.panNo()));
        customer.setAadhaarNo(trim(request.aadhaarNo()));
        String country = trim(request.countryName());
        customer.setCountryName(country == null || country.isBlank() ? "India" : country);

        customer.setHos(trim(request.hos()));
        customer.setAccountGroup(trim(request.accountGroup()));
        customer.setRelationType(trim(request.relationType()));
        customer.setMotherName(trim(request.motherName()));
        customer.setDob(trim(request.dob()));
        customer.setSpouseDob(trim(request.spouseDob()));
        customer.setCaste(trim(request.caste()));
        customer.setQualification(trim(request.qualification()));
        customer.setMaritalStatus(trim(request.maritalStatus()));
        customer.setAnniversaryDate(trim(request.anniversaryDate()));
        customer.setTaxNo(trim(request.taxNo()));
        customer.setBankName(trim(request.bankName()));
        customer.setBankAccName(trim(request.bankAccName()));
        customer.setBankAccNo(trim(request.bankAccNo()));
        customer.setBankIfscCode(trim(request.bankIfscCode()));
        customer.setOccupation(trim(request.occupation()));
        customer.setAnnualIncome(trim(request.annualIncome()));
        customer.setNomineeName(trim(request.nomineeName()));
        customer.setNomineeRelation(trim(request.nomineeRelation()));
        customer.setDesignation(trim(request.designation()));
        customer.setLoginId(trim(request.loginId()));
        // Passwords: store only when explicitly provided; never clear on blank update; never return via API.
        String portalPassword = trim(request.portalPassword());
        if (portalPassword != null && !portalPassword.isBlank()) {
            customer.setPortalPassword(portalPassword);
        }
        String masterPassword = trim(request.masterPassword());
        if (masterPassword != null && !masterPassword.isBlank()) {
            customer.setMasterPassword(masterPassword);
        }
        customer.setBusinessName(trim(request.businessName()));
        customer.setOfficeAddress(trim(request.officeAddress()));
        customer.setPermanentAddress(trim(request.permanentAddress()));
        customer.setCurrentAddress(trim(request.currentAddress()));
        customer.setShippingAddress(trim(request.shippingAddress()));
        customer.setWardNumber(trim(request.wardNumber()));
        customer.setTehsil(trim(request.tehsil()));
        customer.setOtherInfo(trim(request.otherInfo()));

        // Decodes base64 data URL and writes to disk file, storing only short URL path in database
        if (request.photoBase64() != null) {
            customer.setPhotoBase64(storageService.saveIfBase64(customer.getId() + "_photo", request.photoBase64()));
        }
        if (request.aadhaarFrontBase64() != null) {
            customer.setAadhaarFrontBase64(storageService.saveIfBase64(customer.getId() + "_aadhaar_front", request.aadhaarFrontBase64()));
        }
        customer.setAadhaarFrontDesc(trim(request.aadhaarFrontDesc()));
        if (request.aadhaarBackBase64() != null) {
            customer.setAadhaarBackBase64(storageService.saveIfBase64(customer.getId() + "_aadhaar_back", request.aadhaarBackBase64()));
        }
        customer.setAadhaarBackDesc(trim(request.aadhaarBackDesc()));
        if (request.sendWelcomeSms() != null) customer.setSendWelcomeSms(request.sendWelcomeSms());
        customer.setUpdatedAt(Instant.now());
    }

    @Transactional
    public MasterDataDtos.MetalResponse createMetal(MasterDataDtos.MetalRequest request) {
        if (metalRepository.findByCode(request.code()).isPresent()) {
            throw new BusinessException("METAL_CODE_EXISTS", "Metal code already exists");
        }
        Metal metal = new Metal();
        metal.setId(UUID.randomUUID().toString());
        metal.setCode(request.code().trim().toUpperCase());
        metal.setName(request.name().trim());
        metal.setSymbol(request.symbol());
        metalRepository.save(metal);
        return toMetal(metal);
    }

    @Transactional(readOnly = true)
    public List<MasterDataDtos.MetalResponse> listMetals() {
        return metalRepository.findAll().stream().map(this::toMetal).toList();
    }

    @Transactional
    public MasterDataDtos.PurityResponse createPurity(MasterDataDtos.PurityRequest request) {
        metalRepository.findById(request.metalId())
                .orElseThrow(() -> new NotFoundException("Metal not found"));
        if (purityRepository.findByMetalIdAndLabel(request.metalId(), request.label()).isPresent()) {
            throw new BusinessException("PURITY_EXISTS", "Purity label already exists for this metal");
        }
        Purity purity = new Purity();
        purity.setId(UUID.randomUUID().toString());
        purity.setMetalId(request.metalId());
        purity.setLabel(request.label().trim());
        purity.setFineness(request.fineness());
        purityRepository.save(purity);
        return toPurity(purity);
    }

    @Transactional(readOnly = true)
    public List<MasterDataDtos.PurityResponse> listPurities() {
        return purityRepository.findAll().stream().map(this::toPurity).toList();
    }

    @Transactional
    public MasterDataDtos.SupplierResponse createSupplier(MasterDataDtos.SupplierRequest request) {
        String code = trim(request.code());
        if (code == null || code.isBlank()) {
            code = documentNumberService.next("SUPPLIER");
        } else if (supplierRepository.existsByCode(code)) {
            throw new BusinessException("SUPPLIER_CODE_EXISTS", "Supplier code already exists");
        }
        Supplier supplier = new Supplier();
        supplier.setId(UUID.randomUUID().toString());
        supplier.setCode(code);
        supplier.setName(request.name().trim());
        supplier.setPhone(trim(request.phone()));
        supplier.setGstin(trim(request.gstin()));
        supplier.setCity(trim(request.city()));
        supplierRepository.save(supplier);
        return toSupplier(supplier);
    }

    @Transactional(readOnly = true)
    public List<MasterDataDtos.SupplierResponse> listSuppliers() {
        return supplierRepository.findAll().stream().map(this::toSupplier).toList();
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private MasterDataDtos.CustomerSummary toCustomerSummary(Customer c) {
        return new MasterDataDtos.CustomerSummary(
                c.getId(),
                c.getCode(),
                c.getName(),
                c.getFirstName(),
                c.getLastName(),
                c.getPhone(),
                c.getCity(),
                c.getStatus(),
                c.getPartyType(),
                c.getGstin()
        );
    }

    private MasterDataDtos.CustomerResponse toCustomer(Customer c) {
        return new MasterDataDtos.CustomerResponse(
                c.getId(), c.getCode(), c.getName(), c.getFirstName(), c.getLastName(), c.getFatherName(),
                c.getPhone(), c.getEmail(), c.getGstin(), c.getCity(), c.getAddress(), c.getStatus(),
                c.getTitle(), c.getGender(), c.getSpouseName(), c.getAltPhone(), c.getStateName(),
                c.getVillage(), c.getPincode(), c.getPanNo(), c.getAadhaarNo(), c.getCountryName(),
                c.getHos(), c.getAccountGroup(), c.getRelationType(), c.getMotherName(), c.getDob(),
                c.getSpouseDob(), c.getCaste(), c.getQualification(), c.getMaritalStatus(), c.getAnniversaryDate(),
                c.getTaxNo(), c.getBankName(), c.getBankAccName(), c.getBankAccNo(), c.getBankIfscCode(),
                c.getOccupation(), c.getAnnualIncome(), c.getNomineeName(), c.getNomineeRelation(),
                c.getDesignation(), c.getLoginId(),
                null, // portalPassword — never expose
                null, // masterPassword — never expose
                c.getBusinessName(), c.getOfficeAddress(), c.getPermanentAddress(), c.getCurrentAddress(),
                c.getShippingAddress(), c.getWardNumber(), c.getTehsil(), c.getOtherInfo(),
                c.getPhotoBase64(), c.getAadhaarFrontBase64(), c.getAadhaarFrontDesc(),
                c.getAadhaarBackBase64(), c.getAadhaarBackDesc(), c.getSendWelcomeSms()
        );
    }

    private MasterDataDtos.MetalResponse toMetal(Metal m) {
        return new MasterDataDtos.MetalResponse(m.getId(), m.getCode(), m.getName(), m.getSymbol(), m.getWeightUnit(), m.isActive());
    }

    private MasterDataDtos.PurityResponse toPurity(Purity p) {
        return new MasterDataDtos.PurityResponse(p.getId(), p.getMetalId(), p.getLabel(), p.getFineness(), p.isActive());
    }

    private MasterDataDtos.SupplierResponse toSupplier(Supplier s) {
        return new MasterDataDtos.SupplierResponse(s.getId(), s.getCode(), s.getName(), s.getPhone(), s.getGstin(), s.getCity(), s.getStatus());
    }
}
