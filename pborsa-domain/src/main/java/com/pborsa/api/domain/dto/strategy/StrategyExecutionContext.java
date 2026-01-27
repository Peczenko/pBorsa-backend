package com.pborsa.api.domain.dto.strategy;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.Period;
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
        BigDecimal budget,
        Instant start,
        Instant end
) {
    /**
     * Creates execution context with configurable lookback and end offset.
     *
     * @param userId         user ID
     * @param strategyId     strategy ID
     * @param symbol         stock symbol
     * @param budget         strategy budget
     * @param lookbackPeriod lookback period for historical data
     * @param endOffset      offset from current time for end timestamp
     * @return configured execution context
     */
    public static StrategyExecutionContext create(
            Long userId,
            Long strategyId,
            String symbol,
            BigDecimal budget,
            Period lookbackPeriod,
            Duration endOffset
    ) {
        ZonedDateTime nowUtc = ZonedDateTime.now(ZoneOffset.UTC);
        Instant end = nowUtc.minus(endOffset).toInstant();
        Instant start = nowUtc.minus(lookbackPeriod).toInstant();

        return StrategyExecutionContext.builder()
                .executionId(UUID.randomUUID().toString())
                .userId(userId)
                .strategyId(strategyId)
                .symbol(symbol)
                .budget(budget)
                .start(start)
                .end(end)
                .build();
    }
}
