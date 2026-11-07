package com.jewellery.erp.credit.repository;

import com.jewellery.erp.credit.domain.CustomerBalance;
import com.jewellery.erp.credit.domain.PartyLedgerEntry;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface PartyLedgerEntryRepository extends JpaRepository<PartyLedgerEntry, String> {

    List<PartyLedgerEntry> findByCustomerIdOrderByEntryDateDescCreatedAtDesc(String customerId);

    /** Udhaar outstanding = UDHAAR - RECEIPT +/- ADJUSTMENT (adjustment amount is signed). */
    @Query("""
            select coalesce(sum(case
                when e.entryType = 'UDHAAR' then e.amount
                when e.entryType = 'RECEIPT' then -e.amount
                when e.entryType = 'ADJUSTMENT' then e.amount
                else 0 end), 0)
            from PartyLedgerEntry e where e.customerId = :customerId
            """)
    BigDecimal udhaarBalance(@Param("customerId") String customerId);

    @Query("""
            select coalesce(sum(e.amount), 0)
            from PartyLedgerEntry e where e.customerId = :customerId and e.entryType = 'ADVANCE'
            """)
    BigDecimal advanceBalance(@Param("customerId") String customerId);

    @Query("""
            select new com.jewellery.erp.credit.domain.CustomerBalance(e.customerId, sum(case
                when e.entryType = 'UDHAAR' then e.amount
                when e.entryType = 'RECEIPT' then -e.amount
                when e.entryType = 'ADJUSTMENT' then e.amount
                else 0 end))
            from PartyLedgerEntry e
            group by e.customerId
            having sum(case
                when e.entryType = 'UDHAAR' then e.amount
                when e.entryType = 'RECEIPT' then -e.amount
                when e.entryType = 'ADJUSTMENT' then e.amount
                else 0 end) > 0
            order by sum(case
                when e.entryType = 'UDHAAR' then e.amount
                when e.entryType = 'RECEIPT' then -e.amount
                when e.entryType = 'ADJUSTMENT' then e.amount
                else 0 end) desc
            """)
    List<CustomerBalance> topUdhaar(Pageable pageable);

    @Query("""
            select new com.jewellery.erp.credit.domain.CustomerBalance(e.customerId, sum(e.amount))
            from PartyLedgerEntry e
            where e.entryType = 'ADVANCE'
            group by e.customerId
            having sum(e.amount) > 0
            order by sum(e.amount) desc
            """)
    List<CustomerBalance> topAdvance(Pageable pageable);
}
