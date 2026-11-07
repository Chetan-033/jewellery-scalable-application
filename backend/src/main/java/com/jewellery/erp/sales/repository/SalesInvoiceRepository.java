package com.jewellery.erp.sales.repository;

import com.jewellery.erp.sales.domain.SalesInvoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SalesInvoiceRepository extends JpaRepository<SalesInvoice, String> {
    List<SalesInvoice> findAllByOrderByCreatedAtDesc();
}
