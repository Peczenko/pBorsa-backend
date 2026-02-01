package com.pborsa.api.domain.dto.backtest;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * DTO representing a single balance point in a backtest timeline.
 */
public record BacktestBalancePointDto(
        Instant timestamp,
        BigDecimal balance
) {
}
