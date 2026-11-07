package com.jewellery.erp.credit.domain;

import java.math.BigDecimal;

/** Aggregated per-customer balance row (JPQL constructor projection). */
public record CustomerBalance(String customerId, BigDecimal balance) {
}
