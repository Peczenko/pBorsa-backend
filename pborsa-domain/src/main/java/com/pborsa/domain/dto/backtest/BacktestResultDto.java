package com.pborsa.domain.dto.backtest;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO wrapping the result from the trading engine backtest execution.
 */
@Builder
public record BacktestResultDto(
        Long backtestId,
        boolean success,
        String message,
        List<BacktestOrderDto> orders,
        BigDecimal pnl,
        BigDecimal maxDrawdown,
        int totalTrades,
        int winningTrades
) {
}
