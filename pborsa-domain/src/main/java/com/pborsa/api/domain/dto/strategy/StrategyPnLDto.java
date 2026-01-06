package com.pborsa.api.domain.dto.strategy;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * DTO representing profit/loss data for a user strategy.
 */
public record StrategyPnLDto(
        Long strategyId,
        String symbol,
        BigDecimal realizedPnL,
        BigDecimal unrealizedPnL,
        BigDecimal totalPnL,
        BigDecimal totalShares,
        BigDecimal averageCostPerShare,
        BigDecimal currentPrice,
        BigDecimal totalCostBasis,
        Instant updatedAt
) {
    /**
     * Creates a P/L DTO with calculated total P/L.
     */
    public static StrategyPnLDto of(
            Long strategyId,
            String symbol,
            BigDecimal realizedPnL,
            BigDecimal unrealizedPnL,
            BigDecimal totalShares,
            BigDecimal averageCostPerShare,
            BigDecimal currentPrice,
            BigDecimal totalCostBasis,
            Instant updatedAt
    ) {
        BigDecimal totalPnL = (realizedPnL != null ? realizedPnL : BigDecimal.ZERO)
                .add(unrealizedPnL != null ? unrealizedPnL : BigDecimal.ZERO);

        return new StrategyPnLDto(
                strategyId,
                symbol,
                realizedPnL,
                unrealizedPnL,
                totalPnL,
                totalShares,
                averageCostPerShare,
                currentPrice,
                totalCostBasis,
                updatedAt
        );
    }

    /**
     * Creates an empty P/L DTO for strategies with no position.
     */
    public static StrategyPnLDto empty(Long strategyId, String symbol) {
        return new StrategyPnLDto(
                strategyId,
                symbol,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                null,
                BigDecimal.ZERO,
                null
        );
    }
}

