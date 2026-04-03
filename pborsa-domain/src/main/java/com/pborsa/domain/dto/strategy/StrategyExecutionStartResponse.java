package com.pborsa.domain.dto.strategy;

import lombok.Builder;

import java.time.Instant;

/**
 * Response payload for strategy execution start.
 */
@Builder
public record StrategyExecutionStartResponse(
        String executionId,
        Long userId,
        Long strategyId,
        String symbol,
        Instant start,
        Instant end,
        String status
) {
}
