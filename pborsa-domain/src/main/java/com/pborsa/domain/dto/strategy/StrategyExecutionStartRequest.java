package com.pborsa.domain.dto.strategy;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

/**
 * Request payload for starting a strategy execution.
 */
@Builder
public record StrategyExecutionStartRequest(
        @NotBlank(message = "Symbol is required")
        String symbol
) {
}
