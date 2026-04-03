package com.pborsa.api.shared.config.temporal;

import com.pborsa.api.backtest.temporal.activity.BacktestActivitiesImpl;
import com.pborsa.api.order.temporal.activity.OrderStatusUpdateActivitiesImpl;
import com.pborsa.api.strategy.temporal.activity.StrategyExecutionActivitiesImpl;
import com.pborsa.temporal.config.TaskQueues;
import com.pborsa.api.backtest.temporal.workflow.BacktestExecutionWorkflowImpl;
import com.pborsa.api.strategy.temporal.workflow.StrategyExecutionWorkflowImpl;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Registers strategy execution workflow and activities on the configured worker and starts the factory.
 * Mirrors the WorkerRegistration pattern used in the trading worker module.
 */
@Configuration
@ConditionalOnTemporalEnabled
@RequiredArgsConstructor
@Slf4j
public class WorkerRegistration {

    private final WorkerFactory workerFactory;
    private final Worker strategyExecutionWorker;
    private final Worker backtestExecutionWorker;
    private final StrategyExecutionActivitiesImpl activitiesImpl;
    private final OrderStatusUpdateActivitiesImpl orderStatusUpdateActivities;
    private final BacktestActivitiesImpl backtestActivitiesImpl;

    @PostConstruct
    public void register() {
        log.info("Registering strategy execution workflow and activities on queue {}", TaskQueues.STRATEGY_EXECUTION_TASK_QUEUE);
        strategyExecutionWorker.registerWorkflowImplementationTypes(StrategyExecutionWorkflowImpl.class);
        strategyExecutionWorker.registerActivitiesImplementations(activitiesImpl, orderStatusUpdateActivities);

        log.info("Registering backtest execution workflow and activities on queue {}", TaskQueues.BACKTEST_TASK_QUEUE);
        backtestExecutionWorker.registerWorkflowImplementationTypes(BacktestExecutionWorkflowImpl.class);
        backtestExecutionWorker.registerActivitiesImplementations(backtestActivitiesImpl);

        workerFactory.start();
        log.info("Temporal workers started");
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down strategy execution Temporal worker...");
        try {
            workerFactory.shutdown();
            workerFactory.awaitTermination(30, TimeUnit.SECONDS);
            if (!workerFactory.isTerminated()) {
                log.warn("Worker factory did not terminate within timeout, forcing shutdown");
                workerFactory.shutdownNow();
            }
            log.info("Strategy execution Temporal worker shut down successfully");
        } catch (Exception e) {
            log.error("Error during strategy execution worker shutdown", e);
        }
    }
}
