package com.pborsa.api.shared.config.temporal;

import com.pborsa.temporal.config.TaskQueues;
import com.pborsa.temporal.workflow.BacktestExecutionWorkflow;
import com.pborsa.temporal.workflow.BatchTradeExecutionWorkflow;
import com.pborsa.temporal.workflow.MarketDataPollingWorkflow;
import com.pborsa.temporal.workflow.StrategyExecutionWorkflow;
import com.pborsa.temporal.workflow.TradeExecutionWorkflow;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.function.Function;

/**
 * Concrete implementation creating workflow providers with proper WorkflowOptions.
 * Equivalent to ImperioWorkflowsConfiguration in reference project.
 * Only active when Temporal is enabled.
 */
@Configuration
@RequiredArgsConstructor
@ConditionalOnTemporalEnabled
public class TemporalWorkflowsConfiguration extends AbstractTemporalWorkflowsConfiguration {

    private final WorkflowClient workflowClient;
    private final TemporalProperties temporalProperties;

    @Override
    protected <T> Function<String, T> createWorkflowProvider(Class<T> workflowClass) {
        return (String workflowId) -> {
            // Determine task queue based on workflow type
            String taskQueue = determineTaskQueue(workflowClass);
            
            Duration executionTimeout = Duration.ofHours(1);
            Duration runTimeout = Duration.ofMinutes(30);

            if (StrategyExecutionWorkflow.class.isAssignableFrom(workflowClass)) {
                executionTimeout = Duration.ofHours(6);
                runTimeout = Duration.ofHours(6);
            } else if (BacktestExecutionWorkflow.class.isAssignableFrom(workflowClass)) {
                executionTimeout = Duration.ofHours(24);
                runTimeout = Duration.ofHours(24);
            }

            WorkflowOptions options = WorkflowOptions.newBuilder()
                    .setWorkflowId(workflowId)
                    .setTaskQueue(taskQueue)
                    .setWorkflowExecutionTimeout(executionTimeout)
                    .setWorkflowRunTimeout(runTimeout)
                    .setWorkflowTaskTimeout(Duration.ofMinutes(10))
                    .build();
            
            return workflowClient.newWorkflowStub(workflowClass, options);
        };
    }

    /**
     * Determines the appropriate task queue for a workflow class.
     */
    private String determineTaskQueue(Class<?> workflowClass) {
        if (TradeExecutionWorkflow.class.isAssignableFrom(workflowClass) ||
            BatchTradeExecutionWorkflow.class.isAssignableFrom(workflowClass)) {
            return TaskQueues.TRADING_TASK_QUEUE;
        } else if (MarketDataPollingWorkflow.class.isAssignableFrom(workflowClass)) {
            return TaskQueues.MARKET_DATA_TASK_QUEUE;
        } else if (StrategyExecutionWorkflow.class.isAssignableFrom(workflowClass)) {
            return TaskQueues.STRATEGY_EXECUTION_TASK_QUEUE;
        } else if (BacktestExecutionWorkflow.class.isAssignableFrom(workflowClass)) {
            return TaskQueues.BACKTEST_TASK_QUEUE;
        }
        // Default to trading queue
        return TaskQueues.TRADING_TASK_QUEUE;
    }
}
