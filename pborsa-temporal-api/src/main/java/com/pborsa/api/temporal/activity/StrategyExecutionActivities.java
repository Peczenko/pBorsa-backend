package com.pborsa.api.temporal.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

import com.pborsa.api.domain.dto.strategy.StrategyExecutionContext;

/**
 * Activities for strategy execution data streaming.
 */
@ActivityInterface
public interface StrategyExecutionActivities {

    @ActivityMethod
    void streamHistoricalData(StrategyExecutionContext context);
}
