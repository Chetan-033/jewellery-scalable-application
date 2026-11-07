package com.jewellery.erp.masterdata.repository;

import com.jewellery.erp.masterdata.domain.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplierRepository extends JpaRepository<Supplier, String> {
    boolean existsByCode(String code);
}
