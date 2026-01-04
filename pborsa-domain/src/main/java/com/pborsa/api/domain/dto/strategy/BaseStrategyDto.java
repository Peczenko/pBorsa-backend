package com.pborsa.api.domain.dto.strategy;

import java.time.Instant;

/**
 * DTO representing a base strategy template from the catalog.
 */
public record BaseStrategyDto(
        Long id,
        String code,
        String name,
        String description,
        Boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}



