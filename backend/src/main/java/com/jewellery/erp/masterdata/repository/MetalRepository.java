package com.jewellery.erp.masterdata.repository;

import com.jewellery.erp.masterdata.domain.Metal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MetalRepository extends JpaRepository<Metal, String> {
    Optional<Metal> findByCode(String code);
}
