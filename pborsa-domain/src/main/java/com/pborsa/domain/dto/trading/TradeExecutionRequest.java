package com.pborsa.domain.dto.trading;

import lombok.Builder;

import java.util.UUID;

/**
 * Payload for executing a single trade via workflow.
 */
@Builder
public record TradeExecutionRequest(
        Long userId,
        UUID orderId,
        TradingApiOrderRequest order
) {
}
