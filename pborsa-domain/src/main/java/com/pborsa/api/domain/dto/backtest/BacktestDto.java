package com.pborsa.api.domain.dto.backtest;

import com.pborsa.api.domain.dto.strategy.BaseStrategyDto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * DTO representing a backtest with its results.
 */
public record BacktestDto(
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
        String errorMessage,
        Instant createdAt,
        Instant updatedAt,
        Instant completedAt,
        List<BacktestOrderDto> orders
) {
}
