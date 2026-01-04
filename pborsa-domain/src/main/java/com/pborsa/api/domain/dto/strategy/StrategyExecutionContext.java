package com.pborsa.api.domain.dto.strategy;

import lombok.Builder;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Context describing a strategy execution request.
 */
@Builder
public record StrategyExecutionContext(
        String executionId,
        Long userId,
        Long strategyId,
        String symbol,
        Instant start,
        Instant end
) {
    public static StrategyExecutionContext defaultExecutionContext(
            Long userId,
            Long strategyId,
            String symbol
    ) {
        ZonedDateTime nowUtc = ZonedDateTime.now(ZoneOffset.UTC);
        Instant end = nowUtc.minusMinutes(15).toInstant();
        Instant start = nowUtc.minusMonths(3).toInstant();

        return StrategyExecutionContext.builder()
                .executionId(UUID.randomUUID().toString())
                .userId(userId)
                .strategyId(strategyId)
                .symbol(symbol)
                .start(start)
                .end(end)
                .build();
    }
}
