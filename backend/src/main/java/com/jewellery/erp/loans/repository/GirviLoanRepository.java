package com.jewellery.erp.loans.repository;

import com.jewellery.erp.loans.domain.GirviLoan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GirviLoanRepository extends JpaRepository<GirviLoan, String> {
    List<GirviLoan> findByStatusOrderByOpenDateDescCreatedAtDesc(String status);

    List<GirviLoan> findAllByOrderByOpenDateDescCreatedAtDesc();

    List<GirviLoan> findByCustomerIdOrderByOpenDateDescCreatedAtDesc(String customerId);

    Page<GirviLoan> findByStatus(String status, Pageable pageable);

    Page<GirviLoan> findAllBy(Pageable pageable);
}
