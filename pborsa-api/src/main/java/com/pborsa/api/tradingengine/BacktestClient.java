package com.pborsa.api.tradingengine;

import com.pborsa.domain.dto.backtest.BacktestExecutionContext;
import com.pborsa.domain.dto.backtest.BacktestResultDto;
import com.pborsa.domain.dto.market.StockBarDto;

import java.util.List;

/**
 * Client abstraction for executing backtests on the trading engine.
 */
public interface BacktestClient {

    /**
     * Executes a backtest on the trading engine.
     *
     * @param context             Backtest execution context
     * @param historyBeforeStart  Historical bars before testing period (for context)
     * @param historyTestingRange Historical bars during testing period
     * @return Backtest result with orders and metrics
     */
    BacktestResultDto executeBacktest(
            BacktestExecutionContext context,
            List<StockBarDto> historyBeforeStart,
            List<StockBarDto> historyTestingRange
    );

    /**
     * Checks if the backtest client is enabled.
     *
     * @return true if enabled
     */
    default boolean isEnabled() {
        return true;
    }
}
