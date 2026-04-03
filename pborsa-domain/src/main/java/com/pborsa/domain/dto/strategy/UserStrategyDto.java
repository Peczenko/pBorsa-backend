package com.pborsa.domain.dto.strategy;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * DTO representing a user's strategy subscription.
 */
public record UserStrategyDto(
        Long id,
        String name,
        BaseStrategyDto baseStrategy,
        String symbol,
        String status,
        BigDecimal budget,
        Instant createdAt,
        Instant updatedAt
) {
}



