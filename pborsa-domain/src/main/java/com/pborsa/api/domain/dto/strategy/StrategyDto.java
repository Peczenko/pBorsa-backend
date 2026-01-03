package com.pborsa.api.domain.dto.strategy;

import java.time.Instant;

/**
 * DTO representing a strategy.
 */
public record StrategyDto(
        Long id,
        String name,
        String description,
        Boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}


