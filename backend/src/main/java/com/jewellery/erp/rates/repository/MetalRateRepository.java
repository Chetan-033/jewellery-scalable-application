package com.jewellery.erp.rates.repository;

import com.jewellery.erp.rates.domain.MetalRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface MetalRateRepository extends JpaRepository<MetalRate, String> {
    List<MetalRate> findByMetalIdOrderByEffectiveFromDesc(String metalId);

    /** Latest effective rate per (metal, purity) as of {@code now}; may return ties, caller de-duplicates. */
    @Query("""
            select r from MetalRate r
            where r.effectiveFrom <= :now
              and r.effectiveFrom = (
                  select max(r2.effectiveFrom) from MetalRate r2
                  where r2.metalId = r.metalId
                    and coalesce(r2.purityId, '') = coalesce(r.purityId, '')
                    and r2.effectiveFrom <= :now)
            order by r.effectiveFrom desc
            """)
    List<MetalRate> findLatestPerMetalAndPurity(@Param("now") Instant now);
}
