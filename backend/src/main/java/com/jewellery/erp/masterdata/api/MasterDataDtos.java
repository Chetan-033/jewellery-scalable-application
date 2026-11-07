package com.jewellery.erp.masterdata.api;

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
            @Size(max = 100) String countryName
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
            String countryName
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
