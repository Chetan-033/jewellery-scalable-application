package com.jewellery.erp.inventory.application;

import com.jewellery.erp.inventory.api.InventoryDtos;
import com.jewellery.erp.inventory.repository.StockBalanceRepository;
import com.jewellery.erp.inventory.repository.StockItemRepository;
import com.jewellery.erp.shared.domain.MoneyMath;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class StockSummaryService {

    private final StockBalanceRepository stockBalanceRepository;
    private final StockItemRepository stockItemRepository;

    public StockSummaryService(StockBalanceRepository stockBalanceRepository, StockItemRepository stockItemRepository) {
        this.stockBalanceRepository = stockBalanceRepository;
        this.stockItemRepository = stockItemRepository;
    }

    /** Dashboard totals across all branches. itemCount/retailCount = distinct items currently in stock. */
    @Transactional(readOnly = true)
    public InventoryDtos.StockSummaryResponse summary() {
        BigDecimal goldGross = zero();
        BigDecimal goldNet = zero();
        BigDecimal silverGross = zero();
        BigDecimal silverNet = zero();
        for (Object[] row : stockBalanceRepository.sumWeightsByMetal()) {
            String code = row[0] == null ? "" : String.valueOf(row[0]).toUpperCase();
            BigDecimal gross = MoneyMath.weight((BigDecimal) row[1]);
            BigDecimal net = MoneyMath.weight((BigDecimal) row[2]);
            if ("GOLD".equals(code)) {
                goldGross = goldGross.add(gross);
                goldNet = goldNet.add(net);
            } else if ("SILVER".equals(code)) {
                silverGross = silverGross.add(gross);
                silverNet = silverNet.add(net);
            }
        }
        return new InventoryDtos.StockSummaryResponse(
                goldGross, goldNet, silverGross, silverNet,
                stockItemRepository.countInStock(),
                stockItemRepository.countRetailInStock()
        );
    }

    private BigDecimal zero() {
        return BigDecimal.ZERO.setScale(MoneyMath.WEIGHT_SCALE);
    }
}
