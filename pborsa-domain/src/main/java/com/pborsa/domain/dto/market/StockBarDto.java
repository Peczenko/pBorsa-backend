package com.pborsa.domain.dto.market;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * DTO representing an OHLCV bar (candlestick) for a stock.
 */
@Builder
public record StockBarDto(
        String symbol,
        BigDecimal open,
        BigDecimal high,
        BigDecimal low,
        BigDecimal close,
        Long volume,
        Long tradeCount,
        BigDecimal vwap,
        Instant timestamp
) {
    /**
     * Calculates the price change from open to close.
     */
    public BigDecimal priceChange() {
        if (open == null || close == null) {
            return BigDecimal.ZERO;
        }
        return close.subtract(open);
    }

    /**
     * Calculates the percentage change from open to close.
     */
    public BigDecimal priceChangePercent() {
        if (open == null || close == null || open.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return priceChange()
                .divide(open, 6, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    /**
     * Determines if this bar is bullish (close > open).
     */
    public boolean isBullish() {
        return close != null && open != null && close.compareTo(open) > 0;
    }

    /**
     * Determines if this bar is bearish (close < open).
     */
    public boolean isBearish() {
        return close != null && open != null && close.compareTo(open) < 0;
    }
}

