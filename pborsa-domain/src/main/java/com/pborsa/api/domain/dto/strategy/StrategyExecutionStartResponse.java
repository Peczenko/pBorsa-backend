package com.pborsa.api.domain.dto.strategy;

import lombok.Builder;

import java.time.Instant;

/**
 * Response payload for strategy execution start.
 */
@Builder
public record StrategyExecutionStartResponse(
        String executionId,
        Long userId,
        String strategyId,
        String symbol,
        String timeframe,
        Instant start,
        Instant end,
        String status
) {
}
