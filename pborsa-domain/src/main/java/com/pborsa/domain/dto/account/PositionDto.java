package com.pborsa.domain.dto.account;

import lombok.Builder;

import java.math.BigDecimal;

/**
 * DTO representing a position in a user's portfolio.
 */
@Builder
public record PositionDto(
        String assetId,
        String symbol,
        String exchange,
        String assetClass,
        BigDecimal averageEntryPrice,
        BigDecimal quantity,
        String side,
        BigDecimal marketValue,
        BigDecimal costBasis,
        BigDecimal unrealizedPnL,
        BigDecimal unrealizedPnLPercent,
        BigDecimal unrealizedIntradayPnL,
        BigDecimal unrealizedIntradayPnLPercent,
        BigDecimal currentPrice,
        BigDecimal lastDayPrice,
        BigDecimal changeToday
) {
    /**
     * Checks if this is a long position.
     */
    public boolean isLong() {
        return "long".equalsIgnoreCase(side);
    }

    /**
     * Checks if this is a short position.
     */
    public boolean isShort() {
        return "short".equalsIgnoreCase(side);
    }

    /**
     * Checks if the position is profitable.
     */
    public boolean isProfitable() {
        return unrealizedPnL != null && unrealizedPnL.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Returns the absolute quantity regardless of position side.
     */
    public BigDecimal absoluteQuantity() {
        if (quantity == null) return BigDecimal.ZERO;
        return quantity.abs();
    }
}

