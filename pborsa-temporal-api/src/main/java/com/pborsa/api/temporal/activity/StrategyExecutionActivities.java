package com.pborsa.api.temporal.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

import java.time.Instant;

/**
 * Activities for strategy execution data streaming.
 */
@ActivityInterface
public interface StrategyExecutionActivities {

    @ActivityMethod
    void streamHistoricalData(String executionId,
                              String userId,
                              String strategyId,
                              String symbol,
                              String timeframe,
                              Instant start,
                              Instant end);
}
