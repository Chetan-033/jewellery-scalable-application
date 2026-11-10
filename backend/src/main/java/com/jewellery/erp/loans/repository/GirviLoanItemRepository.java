package com.jewellery.erp.loans.repository;

import com.jewellery.erp.loans.domain.GirviLoanItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GirviLoanItemRepository extends JpaRepository<GirviLoanItem, String> {
    List<GirviLoanItem> findByGirviLoanIdOrderByLineNoAsc(String girviLoanId);
}

