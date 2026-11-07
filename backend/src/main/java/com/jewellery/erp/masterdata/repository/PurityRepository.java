package com.jewellery.erp.masterdata.repository;

import com.jewellery.erp.masterdata.domain.Purity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PurityRepository extends JpaRepository<Purity, String> {
    Optional<Purity> findByMetalIdAndLabel(String metalId, String label);
}
