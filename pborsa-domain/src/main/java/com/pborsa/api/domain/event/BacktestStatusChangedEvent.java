package com.pborsa.api.domain.event;

import com.pborsa.api.domain.entity.BacktestStatus;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Event published when a backtest's status changes.
 * Used to trigger side effects like starting execution workflows.
 */
public record BacktestStatusChangedEvent(
        Long backtestId,
        Long userId,
        Long baseStrategyId,
        String baseStrategyCode,
        String symbol,
        BigDecimal budget,
        Instant testingStart,
        Instant testingEnd,
        BacktestStatus oldStatus,
        BacktestStatus newStatus
) {
}
