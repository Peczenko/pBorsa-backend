package com.pborsa.domain.dto.backtest;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * DTO representing an order executed during a backtest.
 */
public record BacktestOrderDto(
        Long id,
        String symbol,
        String side,
        BigDecimal quantity,
        BigDecimal price,
        Instant executedAt,
        Instant createdAt
) {
}
