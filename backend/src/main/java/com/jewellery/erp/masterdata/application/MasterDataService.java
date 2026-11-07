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
import com.jewellery.erp.shared.domain.BusinessException;
import com.jewellery.erp.shared.domain.MoneyMath;
import com.jewellery.erp.shared.domain.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class MasterDataService {

    private final CustomerRepository customerRepository;
    private final MetalRepository metalRepository;
    private final PurityRepository purityRepository;
    private final SupplierRepository supplierRepository;
    private final DocumentNumberService documentNumberService;

    public MasterDataService(
            CustomerRepository customerRepository,
            MetalRepository metalRepository,
            PurityRepository purityRepository,
            SupplierRepository supplierRepository,
            DocumentNumberService documentNumberService) {
        this.customerRepository = customerRepository;
        this.metalRepository = metalRepository;
        this.purityRepository = purityRepository;
        this.supplierRepository = supplierRepository;
        this.documentNumberService = documentNumberService;
    }

    @Transactional
    public MasterDataDtos.CustomerResponse createCustomer(MasterDataDtos.CustomerRequest request) {
        String first = trim(request.firstName());
        String last = trim(request.lastName());
        String name = trim(request.name());
        if (name == null || name.isBlank()) {
            name = ((first == null ? "" : first) + " " + (last == null ? "" : last)).trim();
        }
        if (name == null || name.isBlank()) {
            throw new BusinessException("CUSTOMER_NAME_REQUIRED", "Customer name or first/last name is required");
        }
        String code = trim(request.code());
        if (code == null || code.isBlank()) {
            code = documentNumberService.next("CUSTOMER");
        } else if (customerRepository.existsByCode(code)) {
            throw new BusinessException("CUSTOMER_CODE_EXISTS", "Customer code already exists");
        }

        Customer customer = new Customer();
        customer.setId(UUID.randomUUID().toString());
        customer.setCode(code);
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
        customerRepository.save(customer);
        return toCustomer(customer);
    }

    @Transactional(readOnly = true)
    public List<MasterDataDtos.CustomerResponse> listCustomers(String q) {
        List<Customer> rows = (q == null || q.isBlank())
                ? customerRepository.findAll()
                : customerRepository.search(q.trim());
        return rows.stream().map(this::toCustomer).toList();
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
            throw new BusinessException("PURITY_EXISTS", "Purity label already exists for metal");
        }
        Purity purity = new Purity();
        purity.setId(UUID.randomUUID().toString());
        purity.setMetalId(request.metalId());
        purity.setLabel(request.label().trim());
        purity.setFineness(MoneyMath.purity(request.fineness()));
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

    private MasterDataDtos.CustomerResponse toCustomer(Customer c) {
        return new MasterDataDtos.CustomerResponse(
                c.getId(), c.getCode(), c.getName(), c.getFirstName(), c.getLastName(), c.getFatherName(),
                c.getPhone(), c.getEmail(), c.getGstin(), c.getCity(), c.getAddress(), c.getStatus(),
                c.getTitle(), c.getGender(), c.getSpouseName(), c.getAltPhone(), c.getStateName(),
                c.getVillage(), c.getPincode(), c.getPanNo(), c.getAadhaarNo(), c.getCountryName()
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
