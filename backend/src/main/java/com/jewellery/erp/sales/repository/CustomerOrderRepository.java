package com.jewellery.erp.sales.repository;

import com.jewellery.erp.sales.domain.CustomerOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, String> {
    List<CustomerOrder> findAllByOrderByCreatedAtDesc();
}
