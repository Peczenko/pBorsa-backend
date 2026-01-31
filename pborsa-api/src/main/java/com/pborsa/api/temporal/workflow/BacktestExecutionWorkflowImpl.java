package com.pborsa.api.temporal.workflow;

import com.pborsa.api.domain.dto.backtest.BacktestExecutionContext;
import com.pborsa.api.domain.dto.backtest.BacktestResultDto;
import com.pborsa.api.temporal.activity.BacktestActivities;
import com.pborsa.api.temporal.config.TaskQueues;
import io.temporal.activity.ActivityCancellationType;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Workflow;

import java.time.Duration;

/**
 * Workflow implementation for backtest execution.
 * Orchestrates data download, backtest execution, and result storage.
 */
public class BacktestExecutionWorkflowImpl implements BacktestExecutionWorkflow {

    private final BacktestActivities backtestActivities;
    private final BacktestActivities statusActivities;

    public BacktestExecutionWorkflowImpl() {
        // Long-running activity options for backtest execution
        RetryOptions backtestRetryOptions = RetryOptions.newBuilder()
                .setInitialInterval(Duration.ofSeconds(2))
                .setMaximumInterval(Duration.ofSeconds(30))
                .setBackoffCoefficient(2.0)
                .setMaximumAttempts(3)
                .build();

        ActivityOptions backtestOptions = ActivityOptions.newBuilder()
                .setStartToCloseTimeout(Duration.ofMinutes(30))
                .setScheduleToCloseTimeout(Duration.ofHours(1))
                .setTaskQueue(TaskQueues.BACKTEST_TASK_QUEUE)
                .setRetryOptions(backtestRetryOptions)
                .setCancellationType(ActivityCancellationType.WAIT_CANCELLATION_COMPLETED)
                .setHeartbeatTimeout(Duration.ofMinutes(5))
                .build();

        this.backtestActivities = Workflow.newActivityStub(BacktestActivities.class, backtestOptions);

        // Quick activity options for status updates and result storage
        RetryOptions statusRetryOptions = RetryOptions.newBuilder()
                .setInitialInterval(Duration.ofSeconds(1))
                .setMaximumInterval(Duration.ofSeconds(10))
                .setBackoffCoefficient(2.0)
                .setMaximumAttempts(5)
                .build();

        ActivityOptions statusOptions = ActivityOptions.newBuilder()
                .setStartToCloseTimeout(Duration.ofSeconds(30))
                .setScheduleToCloseTimeout(Duration.ofMinutes(1))
                .setTaskQueue(TaskQueues.BACKTEST_TASK_QUEUE)
                .setRetryOptions(statusRetryOptions)
                .build();

        this.statusActivities = Workflow.newActivityStub(BacktestActivities.class, statusOptions);
    }

    @Override
    public void execute(BacktestExecutionContext context) {
        try {
            // Step 1: Download historical data and execute backtest on trading engine
            BacktestResultDto result = backtestActivities.executeBacktest(context);

            // Step 2: Store results in database
            statusActivities.storeResults(result);

            // Step 3: Mark backtest as completed
            statusActivities.markCompleted(context.backtestId());
        } catch (Exception e) {
            // Mark backtest as failed
            String errorMessage = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            statusActivities.markFailed(context.backtestId(), errorMessage);
            throw e;
        }
    }
}
