package com.pborsa.api.temporal.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

import com.pborsa.api.domain.dto.strategy.StrategyExecutionContext;

/**
 * Activities for strategy execution data streaming.
 */
@ActivityInterface
public interface StrategyExecutionActivities {

    /**
     * Streams historical data to the trading engine.
     *
     * @param context Execution context with user, strategy, and symbol info
     */
    @ActivityMethod
    void streamHistoricalData(StrategyExecutionContext context);

    /**
     * Marks the strategy as active after data transfer is complete.
     * Called at the end of the workflow to transition from PREPARING to ACTIVE.
     *
     * @param strategyId The user strategy ID
     */
    @ActivityMethod
    void markStrategyActive(Long strategyId);

    /**
     * Marks the strategy as failed to start.
     * Called when data streaming or preparation fails.
     *
     * @param strategyId   The user strategy ID
     * @param errorMessage The error message describing the failure
     */
    @ActivityMethod
    void markStrategyStartFailed(Long strategyId, String errorMessage);
}
