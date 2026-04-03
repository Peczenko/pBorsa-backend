package com.pborsa.domain.dto.market;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * DTO representing a stock trade execution.
 */
@Builder
public record StockTradeDto(
        String symbol,
        BigDecimal price,
        BigDecimal size,
        Instant timestamp,
        String exchange,
        String tradeId,
        String tape,
        String conditions
) {
    /**
     * Calculates the total trade value.
     */
    public BigDecimal tradeValue() {
        if (price == null || size == null) {
            return BigDecimal.ZERO;
        }
        return price.multiply(size);
    }
}

