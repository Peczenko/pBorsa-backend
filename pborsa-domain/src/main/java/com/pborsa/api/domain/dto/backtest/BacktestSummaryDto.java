package com.pborsa.api.domain.dto.backtest;

import com.pborsa.api.domain.dto.strategy.BaseStrategyDto;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * DTO representing a backtest summary with order counts.
 */
public record BacktestSummaryDto(
        Long id,
        String name,
        BaseStrategyDto baseStrategy,
        String symbol,
        BigDecimal budget,
        Instant testingStart,
        Instant testingEnd,
        String status,
        BigDecimal pnl,
        BigDecimal maxDrawdown,
        Integer totalTrades,
        Integer winningTrades,
        Integer buyOrdersCount,
        Integer sellOrdersCount,
        String errorMessage,
        Instant createdAt,
        Instant updatedAt,
        Instant completedAt
) {
}
