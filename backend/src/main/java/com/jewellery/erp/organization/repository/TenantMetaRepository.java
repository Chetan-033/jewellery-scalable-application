package com.jewellery.erp.organization.repository;

import com.jewellery.erp.organization.domain.TenantMeta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantMetaRepository extends JpaRepository<TenantMeta, String> {
    boolean existsByCode(String code);
}
