package com.pborsa.domain.dto.backtest;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Context for backtest execution workflow.
 */
@Builder
public record BacktestExecutionContext(
        Long backtestId,
        Long userId,
        Long baseStrategyId,
        String baseStrategyCode,
        String symbol,
        BigDecimal budget,
        Instant testingStart,
        Instant testingEnd
) {
}
