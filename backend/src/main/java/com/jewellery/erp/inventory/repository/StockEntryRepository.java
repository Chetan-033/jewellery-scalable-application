package com.jewellery.erp.inventory.repository;

import com.jewellery.erp.inventory.domain.StockEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockEntryRepository extends JpaRepository<StockEntry, String> {
    List<StockEntry> findAllByOrderByCreatedAtDesc();
}
