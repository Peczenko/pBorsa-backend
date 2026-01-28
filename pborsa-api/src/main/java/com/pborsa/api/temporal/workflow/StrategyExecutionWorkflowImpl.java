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
 * Workflow that triggers strategy execution streaming activity
 * and marks the strategy as active upon completion.
 */
public class StrategyExecutionWorkflowImpl implements StrategyExecutionWorkflow {

    private final StrategyExecutionActivities streamingActivities;
    private final StrategyExecutionActivities statusActivities;

    public StrategyExecutionWorkflowImpl() {
        // Long-running activity options for data streaming
        RetryOptions streamingRetryOptions = RetryOptions.newBuilder()
                .setInitialInterval(Duration.ofSeconds(2))
                .setMaximumInterval(Duration.ofSeconds(30))
                .setBackoffCoefficient(2.0)
                .setMaximumAttempts(3)
                .build();

        ActivityOptions streamingOptions = ActivityOptions.newBuilder()
                .setStartToCloseTimeout(Duration.ofHours(3))
                .setScheduleToCloseTimeout(Duration.ofHours(3))
                .setTaskQueue(TaskQueues.STRATEGY_EXECUTION_TASK_QUEUE)
                .setRetryOptions(streamingRetryOptions)
                .setCancellationType(ActivityCancellationType.WAIT_CANCELLATION_COMPLETED)
                .build();

        this.streamingActivities = Workflow.newActivityStub(StrategyExecutionActivities.class, streamingOptions);

        // Quick activity options for status updates
        RetryOptions statusRetryOptions = RetryOptions.newBuilder()
                .setInitialInterval(Duration.ofSeconds(1))
                .setMaximumInterval(Duration.ofSeconds(10))
                .setBackoffCoefficient(2.0)
                .setMaximumAttempts(5)
                .build();

        ActivityOptions statusOptions = ActivityOptions.newBuilder()
                .setStartToCloseTimeout(Duration.ofSeconds(30))
                .setScheduleToCloseTimeout(Duration.ofMinutes(1))
                .setTaskQueue(TaskQueues.STRATEGY_EXECUTION_TASK_QUEUE)
                .setRetryOptions(statusRetryOptions)
                .build();

        this.statusActivities = Workflow.newActivityStub(StrategyExecutionActivities.class, statusOptions);
    }

    @Override
    public void execute(StrategyExecutionContext context) {
        try {
            // Step 1: Stream historical data to trading engine
            streamingActivities.streamHistoricalData(context);

            // Step 2: Mark strategy as active after data transfer completes
            statusActivities.markStrategyActive(context.strategyId());
        } catch (Exception e) {
            // Mark strategy as failed if data streaming fails
            String errorMessage = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            statusActivities.markStrategyStartFailed(context.strategyId(), errorMessage);
            throw e;
        }
    }
}
