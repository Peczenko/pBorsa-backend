package com.pborsa.api.config.temporal;

import io.temporal.activity.ActivityOptions;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.common.RetryOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import io.temporal.worker.WorkerOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Configuration for Temporal workflow engine.
 * Sets up the connection to Temporal server and worker factories.
 * Follows patterns from reference project for scalability.
 */
@Configuration
@Slf4j
public class TemporalConfiguration {

    public static final String TRADING_TASK_QUEUE = "TRADING_TASK_QUEUE";
    public static final String MARKET_DATA_TASK_QUEUE = "MARKET_DATA_TASK_QUEUE";

    @Value("${spring.temporal.connection.target:localhost:7233}")
    private String temporalTarget;

    @Value("${spring.temporal.namespace:default}")
    private String namespace;

    // Trading activity configuration
    @Value("${temporal.activities.trading.start-to-close-timeout:10m}")
    private String tradingActivityTimeout;

    @Value("${temporal.activities.trading.max-attempts:5}")
    private int tradingMaxAttempts;

    @Value("${temporal.activities.trading.initial-retry-interval:5s}")
    private String tradingInitialRetryInterval;

    @Value("${temporal.activities.trading.backoff-coefficient:2.0}")
    private double tradingBackoffCoefficient;

    // Market data activity configuration
    @Value("${temporal.activities.market-data.start-to-close-timeout:2m}")
    private String marketDataActivityTimeout;

    @Value("${temporal.activities.market-data.max-attempts:2}")
    private int marketDataMaxAttempts;

    @Value("${temporal.activities.market-data.initial-retry-interval:1s}")
    private String marketDataInitialRetryInterval;

    @Value("${temporal.activities.market-data.backoff-coefficient:1.5}")
    private double marketDataBackoffCoefficient;

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
    public WorkflowClient workflowClient(@Qualifier("workflowServiceStubs") WorkflowServiceStubs serviceStubs) {
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

    /**
     * Creates ActivityOptions bean for trading activities.
     * Configured with longer timeout and more retries for trading operations.
     */
    @Bean("tradingActivityOptions")
    public ActivityOptions tradingActivityOptions() {
        RetryOptions retryOptions = RetryOptions.newBuilder()
                .setInitialInterval(parseDuration(tradingInitialRetryInterval))
                .setMaximumInterval(Duration.ofSeconds(30))
                .setBackoffCoefficient(tradingBackoffCoefficient)
                .setMaximumAttempts(tradingMaxAttempts)
                .build();

        return ActivityOptions.newBuilder()
                .setStartToCloseTimeout(parseDuration(tradingActivityTimeout))
                .setTaskQueue(TRADING_TASK_QUEUE)
                .setRetryOptions(retryOptions)
                .build();
    }

    /**
     * Creates ActivityOptions bean for market data activities.
     * Configured with shorter timeout and fewer retries for market data operations.
     */
    @Bean("marketDataActivityOptions")
    public ActivityOptions marketDataActivityOptions() {
        RetryOptions retryOptions = RetryOptions.newBuilder()
                .setInitialInterval(parseDuration(marketDataInitialRetryInterval))
                .setMaximumInterval(Duration.ofSeconds(10))
                .setBackoffCoefficient(marketDataBackoffCoefficient)
                .setMaximumAttempts(marketDataMaxAttempts)
                .build();

        return ActivityOptions.newBuilder()
                .setStartToCloseTimeout(parseDuration(marketDataActivityTimeout))
                .setTaskQueue(MARKET_DATA_TASK_QUEUE)
                .setRetryOptions(retryOptions)
                .build();
    }

    /**
     * Creates the trading task queue worker.
     * Only created when Temporal is enabled.
     */
    @Bean
    @ConditionalOnTemporalEnabled
    public Worker tradingWorker(WorkerFactory workerFactory) {
        log.info("Creating trading worker for queue: {} with max concurrent activities: {}, workflows: {}",
                TRADING_TASK_QUEUE, tradingMaxConcurrentActivities, tradingMaxConcurrentWorkflows);
        
        WorkerOptions options = WorkerOptions.newBuilder()
                .setMaxConcurrentWorkflowTaskPollers(4)
                .setMaxConcurrentActivityTaskPollers(4)
                .setMaxConcurrentActivityExecutionSize(tradingMaxConcurrentActivities)
                .setMaxConcurrentWorkflowTaskExecutionSize(tradingMaxConcurrentWorkflows)
                .build();
        
        return workerFactory.newWorker(TRADING_TASK_QUEUE, options);
    }

    /**
     * Creates the market data task queue worker.
     * Only created when Temporal is enabled.
     */
    @Bean
    @ConditionalOnTemporalEnabled
    public Worker marketDataWorker(WorkerFactory workerFactory) {
        log.info("Creating market data worker for queue: {} with max concurrent activities: {}, workflows: {}",
                MARKET_DATA_TASK_QUEUE, marketDataMaxConcurrentActivities, marketDataMaxConcurrentWorkflows);
        
        WorkerOptions options = WorkerOptions.newBuilder()
                .setMaxConcurrentActivityExecutionSize(marketDataMaxConcurrentActivities)
                .setMaxConcurrentWorkflowTaskExecutionSize(marketDataMaxConcurrentWorkflows)
                .build();
        
        return workerFactory.newWorker(MARKET_DATA_TASK_QUEUE, options);
    }

    /**
     * Parses duration string (e.g., "10m", "30s", "1h") to Duration.
     */
    private Duration parseDuration(String durationStr) {
        if (durationStr == null || durationStr.isEmpty()) {
            return Duration.ofMinutes(2);
        }
        
        durationStr = durationStr.trim().toLowerCase();
        if (durationStr.endsWith("s")) {
            return Duration.ofSeconds(Long.parseLong(durationStr.substring(0, durationStr.length() - 1)));
        } else if (durationStr.endsWith("m")) {
            return Duration.ofMinutes(Long.parseLong(durationStr.substring(0, durationStr.length() - 1)));
        } else if (durationStr.endsWith("h")) {
            return Duration.ofHours(Long.parseLong(durationStr.substring(0, durationStr.length() - 1)));
        } else {
            // Try parsing as seconds
            return Duration.ofSeconds(Long.parseLong(durationStr));
        }
    }
}

