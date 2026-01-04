package com.pborsa.api.domain.dto.strategy;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating a user strategy subscription.
 */
public record CreateUserStrategyRequest(
        @NotBlank(message = "Base strategy code is required")
        String baseStrategyCode,

        @NotBlank(message = "Name is required")
        @Size(max = 128, message = "Name must be at most 128 characters")
        String name,

        @NotBlank(message = "Symbol is required")
        @Size(max = 16, message = "Symbol must be at most 16 characters")
        String symbol
) {
}



