package com.pborsa.worker.config.temporal;

import com.pborsa.temporal.config.TaskQueues;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import io.temporal.worker.WorkerOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Temporal workflow engine.
 * Sets up the connection to Temporal server and worker factories.
 * Follows patterns from reference project for scalability.
 */
@Configuration
@Slf4j
public class TemporalConfiguration {

    // Task queues moved to TaskQueues constants class
    // Use TaskQueues.TRADING_TASK_QUEUE and TaskQueues.MARKET_DATA_TASK_QUEUE instead

    @Value("${spring.temporal.connection.target:localhost:7233}")
    private String temporalTarget;

    @Value("${spring.temporal.namespace:default}")
    private String namespace;

    // Workflow configuration
    @Value("${temporal.workflows.execution-timeout:1h}")
    private String workflowExecutionTimeout;

    @Value("${temporal.workflows.run-timeout:30m}")
    private String workflowRunTimeout;

    @Value("${temporal.workflows.task-timeout:10m}")
    private String workflowTaskTimeout;

    // Worker configuration
    @Value("${temporal.workers.trading.max-concurrent-activities:400}")
    private int tradingMaxConcurrentActivities;

    @Value("${temporal.workers.trading.max-concurrent-workflows:200}")
    private int tradingMaxConcurrentWorkflows;

    @Value("${temporal.workers.market-data.max-concurrent-activities:100}")
    private int marketDataMaxConcurrentActivities;

    @Value("${temporal.workers.market-data.max-concurrent-workflows:50}")
    private int marketDataMaxConcurrentWorkflows;

    /**
     * Creates the Temporal service stubs for connecting to the server.
     */
    @Bean
    public WorkflowServiceStubs workflowServiceStubs() {
        log.info("Creating Temporal service stubs connecting to: {}", temporalTarget);
        
        WorkflowServiceStubsOptions options = WorkflowServiceStubsOptions.newBuilder()
                .setTarget(temporalTarget)
                .build();
        
        return WorkflowServiceStubs.newServiceStubs(options);
    }

    /**
     * Creates the Temporal workflow client.
     */
    @Bean
    public WorkflowClient workflowClient(WorkflowServiceStubs serviceStubs) {
        log.info("Creating Temporal workflow client for namespace: {}", namespace);
        
        WorkflowClientOptions options = WorkflowClientOptions.newBuilder()
                .setNamespace(namespace)
                .build();
        
        return WorkflowClient.newInstance(serviceStubs, options);
    }

    /**
     * Creates the worker factory for creating workers.
     */
    @Bean
    public WorkerFactory workerFactory(WorkflowClient workflowClient) {
        return WorkerFactory.newInstance(workflowClient);
    }

    // ActivityOptions beans are provided by TemporalActivityOptionsConfiguration in pborsa-trading module
    // No need to define them here to avoid duplicate bean definitions

    /**
     * Creates the trading task queue worker.
     * Only created when Temporal is enabled.
     */
    @Bean
    public Worker tradingWorker(WorkerFactory workerFactory) {
        log.info("Creating trading worker for queue: {} with max concurrent activities: {}, workflows: {}",
                TaskQueues.TRADING_TASK_QUEUE, tradingMaxConcurrentActivities, tradingMaxConcurrentWorkflows);
        
        WorkerOptions options = WorkerOptions.newBuilder()
                .setMaxConcurrentWorkflowTaskPollers(4)
                .setMaxConcurrentActivityTaskPollers(4)
                .setMaxConcurrentActivityExecutionSize(tradingMaxConcurrentActivities)
                .setMaxConcurrentWorkflowTaskExecutionSize(tradingMaxConcurrentWorkflows)
                .build();
        
        return workerFactory.newWorker(TaskQueues.TRADING_TASK_QUEUE, options);
    }

    /**
     * Creates the market data task queue worker.
     * Only created when Temporal is enabled.
     */
    @Bean
    public Worker marketDataWorker(WorkerFactory workerFactory) {
        log.info("Creating market data worker for queue: {} with max concurrent activities: {}, workflows: {}",
                TaskQueues.MARKET_DATA_TASK_QUEUE, marketDataMaxConcurrentActivities, marketDataMaxConcurrentWorkflows);
        
        WorkerOptions options = WorkerOptions.newBuilder()
                .setMaxConcurrentWorkflowTaskPollers(4)
                .setMaxConcurrentActivityTaskPollers(4)
                .setMaxConcurrentActivityExecutionSize(marketDataMaxConcurrentActivities)
                .setMaxConcurrentWorkflowTaskExecutionSize(marketDataMaxConcurrentWorkflows)
                .build();
        
        return workerFactory.newWorker(TaskQueues.MARKET_DATA_TASK_QUEUE, options);
    }

}

