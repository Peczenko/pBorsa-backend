package com.pborsa.api.temporal.workflow;

import com.pborsa.api.temporal.activity.StrategyExecutionActivities;
import com.pborsa.api.temporal.config.TaskQueues;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Workflow;

import java.time.Duration;
import java.time.Instant;

/**
 * Workflow that triggers strategy execution streaming activity.
 */
public class StrategyExecutionWorkflowImpl implements StrategyExecutionWorkflow {

    private final StrategyExecutionActivities activities;

    public StrategyExecutionWorkflowImpl() {
        RetryOptions retryOptions = RetryOptions.newBuilder()
                .setInitialInterval(Duration.ofSeconds(2))
                .setMaximumInterval(Duration.ofSeconds(30))
                .setBackoffCoefficient(2.0)
                .setMaximumAttempts(3)
                .build();

        ActivityOptions options = ActivityOptions.newBuilder()
                .setStartToCloseTimeout(Duration.ofMinutes(15))
                .setTaskQueue(TaskQueues.STRATEGY_EXECUTION_TASK_QUEUE)
                .setRetryOptions(retryOptions)
                .build();

        this.activities = Workflow.newActivityStub(StrategyExecutionActivities.class, options);
    }

    @Override
    public void execute(String executionId,
                        Long userId,
                        String strategyId,
                        String symbol,
                        String timeframe,
                        Instant start,
                        Instant end) {
        activities.streamHistoricalData(executionId, userId, strategyId, symbol, timeframe, start, end);
    }
}
