package com.pborsa.api.domain.dto.strategy;

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
        Instant createdAt,
        Instant updatedAt
) {
}



