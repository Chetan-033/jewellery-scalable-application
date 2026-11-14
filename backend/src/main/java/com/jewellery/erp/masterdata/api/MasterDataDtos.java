package com.jewellery.erp.masterdata.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public final class MasterDataDtos {

    private MasterDataDtos() {
    }

    public record CustomerRequest(
            @Size(max = 64) String code,
            @Size(max = 255) String name,
            @Size(max = 100) String firstName,
            @Size(max = 100) String lastName,
            @Size(max = 100) String fatherName,
            @Size(max = 32) String phone,
            @Size(max = 255) String email,
            @Size(max = 20) String gstin,
            @Size(max = 100) String city,
            @Size(max = 500) String address,
            @Size(max = 16) String title,
            @Size(max = 8) String gender,
            @Size(max = 100) String spouseName,
            @Size(max = 32) String altPhone,
            @Size(max = 100) String stateName,
            @Size(max = 100) String village,
            @Size(max = 16) String pincode,
            @Size(max = 20) String panNo,
            @Size(max = 20) String aadhaarNo,
            @Size(max = 100) String countryName,
            // Extended fields
            @Size(max = 64) String hos,
            @Size(max = 64) String accountGroup,
            @Size(max = 16) String relationType,
            @Size(max = 100) String motherName,
            @Size(max = 32) String dob,
            @Size(max = 32) String spouseDob,
            @Size(max = 64) String caste,
            @Size(max = 100) String qualification,
            @Size(max = 32) String maritalStatus,
            @Size(max = 32) String anniversaryDate,
            @Size(max = 64) String taxNo,
            @Size(max = 100) String bankName,
            @Size(max = 100) String bankAccName,
            @Size(max = 64) String bankAccNo,
            @Size(max = 32) String bankIfscCode,
            @Size(max = 100) String occupation,
            @Size(max = 64) String annualIncome,
            @Size(max = 100) String nomineeName,
            @Size(max = 64) String nomineeRelation,
            @Size(max = 100) String designation,
            @Size(max = 64) String loginId,
            @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
            @Size(max = 128) String portalPassword,
            @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
            @Size(max = 128) String masterPassword,
            @Size(max = 255) String businessName,
            @Size(max = 500) String officeAddress,
            @Size(max = 500) String permanentAddress,
            @Size(max = 500) String currentAddress,
            @Size(max = 500) String shippingAddress,
            @Size(max = 32) String wardNumber,
            @Size(max = 100) String tehsil,
            @Size(max = 1000) String otherInfo,
            String photoBase64,
            String aadhaarFrontBase64,
            @Size(max = 255) String aadhaarFrontDesc,
            String aadhaarBackBase64,
            @Size(max = 255) String aadhaarBackDesc,
            Boolean sendWelcomeSms
    ) {
    }

    /** Light row for grids / pickers — no KYC blobs or passwords. */
    public record CustomerSummary(
            String id,
            String code,
            String name,
            String firstName,
            String lastName,
            String phone,
            String city,
            String status,
            String partyType,
            String gstin
    ) {
    }

    public record CustomerResponse(
            String id,
            String code,
            String name,
            String firstName,
            String lastName,
            String fatherName,
            String phone,
            String email,
            String gstin,
            String city,
            String address,
            String status,
            String title,
            String gender,
            String spouseName,
            String altPhone,
            String stateName,
            String village,
            String pincode,
            String panNo,
            String aadhaarNo,
            String countryName,
            // Extended fields
            String hos,
            String accountGroup,
            String relationType,
            String motherName,
            String dob,
            String spouseDob,
            String caste,
            String qualification,
            String maritalStatus,
            String anniversaryDate,
            String taxNo,
            String bankName,
            String bankAccName,
            String bankAccNo,
            String bankIfscCode,
            String occupation,
            String annualIncome,
            String nomineeName,
            String nomineeRelation,
            String designation,
            String loginId,
            @JsonIgnore String portalPassword,
            @JsonIgnore String masterPassword,
            String businessName,
            String officeAddress,
            String permanentAddress,
            String currentAddress,
            String shippingAddress,
            String wardNumber,
            String tehsil,
            String otherInfo,
            String photoBase64,
            String aadhaarFrontBase64,
            String aadhaarFrontDesc,
            String aadhaarBackBase64,
            String aadhaarBackDesc,
            Boolean sendWelcomeSms
    ) {
    }

    public record MetalRequest(
            @NotBlank @Size(max = 32) String code,
            @NotBlank @Size(max = 128) String name,
            @Size(max = 16) String symbol
    ) {
    }

    public record MetalResponse(
            String id,
            String code,
            String name,
            String symbol,
            String weightUnit,
            boolean active
    ) {
    }

    public record PurityRequest(
            @NotBlank String metalId,
            @NotBlank @Size(max = 64) String label,
            @NotNull BigDecimal fineness
    ) {
    }

    public record PurityResponse(
            String id,
            String metalId,
            String label,
            BigDecimal fineness,
            boolean active
    ) {
    }

    public record SupplierRequest(
            @Size(max = 64) String code,
            @NotBlank @Size(max = 255) String name,
            @Size(max = 32) String phone,
            @Size(max = 20) String gstin,
            @Size(max = 100) String city
    ) {
    }

    public record SupplierResponse(
            String id,
            String code,
            String name,
            String phone,
            String gstin,
            String city,
            String status
    ) {
    }
}
