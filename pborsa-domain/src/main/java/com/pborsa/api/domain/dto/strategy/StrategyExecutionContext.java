package com.pborsa.api.domain.dto.strategy;

import lombok.Builder;

import java.time.Instant;

/**
 * Context describing a strategy execution request.
 */
@Builder
public record StrategyExecutionContext(
        String executionId,
        Long userId,
        String strategyId,
        String symbol,
        String timeframe,
        Instant start,
        Instant end
) {
}
