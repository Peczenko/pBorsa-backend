package com.pborsa.api.shared.config.temporal;

import com.pborsa.temporal.config.TaskQueues;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import io.temporal.worker.WorkerOptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Temporal worker configuration for API-hosted workflows.
 * Structured similarly to the trading worker's TemporalConfiguration so we can add more workers as we grow.
 */
@Configuration
@ConditionalOnTemporalEnabled
@RequiredArgsConstructor
@Slf4j
public class TemporalWorkersConfiguration {

    private final WorkerFactory workerFactory;

    @Value("${temporal.workers.strategy.max-concurrent-activities:50}")
    private int strategyMaxConcurrentActivities;

    @Value("${temporal.workers.strategy.max-concurrent-workflows:25}")
    private int strategyMaxConcurrentWorkflows;

    @Value("${temporal.workers.backtest.max-concurrent-activities:10}")
    private int backtestMaxConcurrentActivities;

    @Value("${temporal.workers.backtest.max-concurrent-workflows:10}")
    private int backtestMaxConcurrentWorkflows;

    /**
     * Worker for strategy execution task queue.
     */
    @Bean
    public Worker strategyExecutionWorker() {
        log.info("Creating strategy execution worker for queue: {} with max concurrent activities: {}, workflows: {}",
                TaskQueues.STRATEGY_EXECUTION_TASK_QUEUE, strategyMaxConcurrentActivities, strategyMaxConcurrentWorkflows);

        WorkerOptions options = WorkerOptions.newBuilder()
                .setMaxConcurrentWorkflowTaskPollers(4)
                .setMaxConcurrentActivityTaskPollers(4)
                .setMaxConcurrentActivityExecutionSize(strategyMaxConcurrentActivities)
                .setMaxConcurrentWorkflowTaskExecutionSize(strategyMaxConcurrentWorkflows)
                .build();

        return workerFactory.newWorker(TaskQueues.STRATEGY_EXECUTION_TASK_QUEUE, options);
    }

    /**
     * Worker for backtest execution task queue.
     */
    @Bean
    public Worker backtestExecutionWorker() {
        log.info("Creating backtest execution worker for queue: {} with max concurrent activities: {}, workflows: {}",
                TaskQueues.BACKTEST_TASK_QUEUE, backtestMaxConcurrentActivities, backtestMaxConcurrentWorkflows);

        WorkerOptions options = WorkerOptions.newBuilder()
                .setMaxConcurrentWorkflowTaskPollers(2)
                .setMaxConcurrentActivityTaskPollers(2)
                .setMaxConcurrentActivityExecutionSize(backtestMaxConcurrentActivities)
                .setMaxConcurrentWorkflowTaskExecutionSize(backtestMaxConcurrentWorkflows)
                .build();

        return workerFactory.newWorker(TaskQueues.BACKTEST_TASK_QUEUE, options);
    }
}
