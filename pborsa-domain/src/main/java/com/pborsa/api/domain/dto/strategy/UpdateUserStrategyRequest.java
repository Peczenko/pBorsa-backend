package com.pborsa.api.domain.dto.strategy;

import jakarta.validation.constraints.Size;

/**
 * Request DTO for updating a user strategy subscription.
 * All fields are optional - only provided fields will be updated.
 */
public record UpdateUserStrategyRequest(
        @Size(max = 128, message = "Name must be at most 128 characters")
        String name,

        String status  // ACTIVE, PAUSED, STOPPED
) {
}



