package com.jewellery.erp.masterdata.repository;

import com.jewellery.erp.masterdata.domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, String> {
    boolean existsByCode(String code);

    @Query("""
            select c from Customer c
            where lower(c.name) like lower(concat('%', :q, '%'))
               or lower(c.code) like lower(concat('%', :q, '%'))
               or lower(coalesce(c.phone, '')) like lower(concat('%', :q, '%'))
               or lower(coalesce(c.city, '')) like lower(concat('%', :q, '%'))
            order by c.name
            """)
    List<Customer> search(@Param("q") String q);
}
