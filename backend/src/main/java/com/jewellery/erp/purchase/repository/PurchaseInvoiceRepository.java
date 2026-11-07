package com.jewellery.erp.purchase.repository;

import com.jewellery.erp.purchase.domain.PurchaseInvoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PurchaseInvoiceRepository extends JpaRepository<PurchaseInvoice, String> {
    List<PurchaseInvoice> findAllByOrderByCreatedAtDesc();
}
