package com.pborsa.domain.dto.backtest;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Request DTO for creating a backtest.
 */
public record CreateBacktestRequest(
        @NotBlank(message = "Base strategy code is required")
        String baseStrategyCode,

        @NotBlank(message = "Name is required")
        @Size(max = 128, message = "Name must be at most 128 characters")
        String name,

        @NotBlank(message = "Symbol is required")
        @Size(max = 16, message = "Symbol must be at most 16 characters")
        String symbol,

        @NotNull(message = "Budget is required")
        @DecimalMin(value = "0.01", message = "Budget must be greater than 0")
        BigDecimal budget,

        @NotNull(message = "Testing start date is required")
        Instant testingStart,

        @NotNull(message = "Testing end date is required")
        Instant testingEnd
) {
}
