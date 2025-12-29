package com.pborsa.api.temporal.workflow;

import com.pborsa.api.domain.dto.strategy.StrategyExecutionContext;
import com.pborsa.api.temporal.activity.StrategyExecutionActivities;
import com.pborsa.api.temporal.config.TaskQueues;
import io.temporal.activity.ActivityCancellationType;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Workflow;

import java.time.Duration;

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
                .setStartToCloseTimeout(Duration.ofHours(3))
                .setScheduleToCloseTimeout(Duration.ofHours(3))
                .setTaskQueue(TaskQueues.STRATEGY_EXECUTION_TASK_QUEUE)
                .setRetryOptions(retryOptions)
                .setCancellationType(ActivityCancellationType.WAIT_CANCELLATION_COMPLETED)
                .build();

        this.activities = Workflow.newActivityStub(StrategyExecutionActivities.class, options);
    }

    @Override
    public void execute(StrategyExecutionContext context) {
        activities.streamHistoricalData(context);
    }
}
