package com.jewellery.erp.sales.repository;

import com.jewellery.erp.sales.domain.SaleExchangeLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface SaleExchangeLineRepository extends JpaRepository<SaleExchangeLine, String> {
    List<SaleExchangeLine> findBySalesInvoiceIdInOrderByLineNoAsc(Collection<String> salesInvoiceIds);
}
