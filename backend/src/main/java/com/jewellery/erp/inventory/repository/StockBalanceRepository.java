package com.jewellery.erp.inventory.repository;

import com.jewellery.erp.inventory.domain.StockBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface StockBalanceRepository extends JpaRepository<StockBalance, String> {
    Optional<StockBalance> findByStockItemIdAndBranchId(String stockItemId, String branchId);

    List<StockBalance> findByStockItemIdIn(Collection<String> stockItemIds);

    /** Rows: [upper(metal code), sum(gross), sum(net)] joined stock_balance -> stock_item -> metal. */
    @Query("""
            select upper(m.code), coalesce(sum(b.grossWeight), 0), coalesce(sum(b.netWeight), 0)
            from StockBalance b
            join StockItem i on i.id = b.stockItemId
            join Metal m on m.id = i.metalId
            group by upper(m.code)
            """)
    List<Object[]> sumWeightsByMetal();
}
