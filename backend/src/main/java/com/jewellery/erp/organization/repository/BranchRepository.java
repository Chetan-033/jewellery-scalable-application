package com.jewellery.erp.organization.repository;

import com.jewellery.erp.organization.domain.Branch;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BranchRepository extends JpaRepository<Branch, String> {
}
