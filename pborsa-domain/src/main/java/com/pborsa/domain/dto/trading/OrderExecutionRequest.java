package com.pborsa.domain.dto.trading;

import lombok.Builder;

/**
 * Request payload for starting an order execution.
 */
@Builder
public record OrderExecutionRequest(
        Long userId,
        TradingApiOrderRequest order
) {
}
