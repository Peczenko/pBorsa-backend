package com.pborsa.domain.dto.market;

import lombok.Builder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

/**
 * DTO representing a stock quote with bid/ask prices.
 */
@Builder
public record StockQuoteDto(
        String symbol,
        BigDecimal bidPrice,
        BigDecimal bidSize,
        BigDecimal askPrice,
        BigDecimal askSize,
        Instant timestamp,
        String bidExchange,
        String askExchange,
        String tape,
        String conditions
) {
    /**
     * Calculates the bid-ask spread.
     */
    public BigDecimal spread() {
        if (askPrice == null || bidPrice == null) {
            return BigDecimal.ZERO;
        }
        return askPrice.subtract(bidPrice);
    }

    /**
     * Calculates the mid-price.
     */
    public BigDecimal midPrice() {
        if (askPrice == null || bidPrice == null) {
            return BigDecimal.ZERO;
        }
        return askPrice.add(bidPrice).divide(BigDecimal.valueOf(2), RoundingMode.HALF_UP);
    }
}

