package com.jewellery.erp.inventory.repository;

import com.jewellery.erp.inventory.domain.StockItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StockItemRepository extends JpaRepository<StockItem, String> {
    boolean existsBySku(String sku);

    /** Exact sku/barcode/rfid matches first, then partial match on sku/barcode/rfid/name. */
    @Query("""
            select i from StockItem i
            where lower(i.sku) like lower(concat('%', :q, '%'))
               or lower(coalesce(i.barcode, '')) like lower(concat('%', :q, '%'))
               or lower(coalesce(i.rfidNo, '')) like lower(concat('%', :q, '%'))
               or lower(i.name) like lower(concat('%', :q, '%'))
            order by case when lower(i.sku) = lower(:q)
                            or lower(coalesce(i.barcode, '')) = lower(:q)
                            or lower(coalesce(i.rfidNo, '')) = lower(:q) then 0 else 1 end,
                     i.name
            """)
    List<StockItem> lookup(@Param("q") String q, Pageable pageable);

    /** Items that currently hold stock (quantity > 0 in at least one branch). */
    @Query("""
            select count(i) from StockItem i
            where exists (select 1 from StockBalance b where b.stockItemId = i.id and b.quantity > 0)
            """)
    long countInStock();

    @Query("""
            select count(i) from StockItem i
            where upper(i.stockType) = 'RETAIL'
              and exists (select 1 from StockBalance b where b.stockItemId = i.id and b.quantity > 0)
            """)
    long countRetailInStock();
}
