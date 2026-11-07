package com.jewellery.erp.inventory.repository;

import com.jewellery.erp.inventory.domain.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockMovementRepository extends JpaRepository<StockMovement, String> {
}
