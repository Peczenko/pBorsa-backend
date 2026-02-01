package com.pborsa.api.domain.event;

import com.pborsa.api.domain.entity.UserStrategyStatus;

import java.math.BigDecimal;

/**
 * Event published when a user strategy's status changes.
 * Used to trigger side effects like starting/stopping execution workflows.
 */
public record StrategyStatusChangedEvent(
        Long strategyId,
        Long userId,
        String symbol,
        String baseStrategyCode,
        BigDecimal budget,
        UserStrategyStatus oldStatus,
        UserStrategyStatus newStatus
) {}

