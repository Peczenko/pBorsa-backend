package com.pborsa.temporal.activity;

import com.pborsa.domain.dto.backtest.BacktestExecutionContext;
import com.pborsa.domain.dto.backtest.BacktestResultDto;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

/**
 * Activities for backtest execution.
 */
@ActivityInterface
public interface BacktestActivities {

    /**
     * Downloads historical data and executes the backtest on the trading engine.
     *
     * @param context Backtest execution context
     * @return Backtest result with orders and metrics
     */
    @ActivityMethod
    BacktestResultDto executeBacktest(BacktestExecutionContext context);

    /**
     * Stores backtest results in the database.
     *
     * @param result Backtest result to store
     */
    @ActivityMethod
    void storeResults(BacktestResultDto result);

    /**
     * Marks the backtest as completed.
     *
     * @param backtestId Backtest ID
     */
    @ActivityMethod
    void markCompleted(Long backtestId);

    /**
     * Marks the backtest as failed.
     *
     * @param backtestId   Backtest ID
     * @param errorMessage Error message describing the failure
     */
    @ActivityMethod
    void markFailed(Long backtestId, String errorMessage);
}
