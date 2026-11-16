package com.jewellery.erp.masterdata.repository;

import com.jewellery.erp.masterdata.domain.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, String> {
    boolean existsByCode(String code);

    @Query("""
            select c from Customer c
            where lower(c.name) like lower(concat('%', :q, '%'))
               or lower(coalesce(c.firstName, '')) like lower(concat('%', :q, '%'))
               or lower(coalesce(c.lastName, '')) like lower(concat('%', :q, '%'))
               or lower(c.code) like lower(concat('%', :q, '%'))
               or lower(coalesce(c.phone, '')) like lower(concat('%', :q, '%'))
               or lower(coalesce(c.city, '')) like lower(concat('%', :q, '%'))
            order by
               case
                  when lower(c.name) like lower(concat(:q, '%')) then 0
                  when lower(coalesce(c.firstName, '')) like lower(concat(:q, '%')) then 1
                  when lower(coalesce(c.lastName, '')) like lower(concat(:q, '%')) then 2
                  when lower(c.code) like lower(concat(:q, '%')) then 3
                  else 4
               end,
               c.name asc
            """)
    List<Customer> search(@Param("q") String q);

    @Query("""
            select c from Customer c
            where (:q is null or :q = ''
               or lower(c.name) like lower(concat('%', :q, '%'))
               or lower(c.code) like lower(concat('%', :q, '%'))
               or lower(coalesce(c.phone, '')) like lower(concat('%', :q, '%'))
               or lower(coalesce(c.city, '')) like lower(concat('%', :q, '%')))
            """)
    Page<Customer> searchPage(@Param("q") String q, Pageable pageable);
}
