package com.pborsa.api.temporal.config;

import com.pborsa.api.config.temporal.ConditionalOnTemporalEnabled;
import com.pborsa.api.config.temporal.TemporalConfiguration;
import com.pborsa.api.temporal.activity.TradingActivities;
import com.pborsa.api.temporal.activity.TradingActivitiesImpl;
import com.pborsa.api.temporal.workflow.BatchTradeExecutionWorkflowImpl;
import com.pborsa.api.temporal.workflow.MarketDataPollingWorkflowImpl;
import com.pborsa.api.temporal.workflow.TradeExecutionWorkflowImpl;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Registers workflows and activities with Temporal workers.
 * Follows scalable patterns from reference project.
 * Only active when Temporal is enabled.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
@ConditionalOnTemporalEnabled
public class WorkerRegistration {

    private final WorkerFactory workerFactory;
    private final Worker tradingWorker;
    private final Worker marketDataWorker;
    private final TradingActivitiesImpl tradingActivitiesImpl;

    @PostConstruct
    public void registerWorkflowsAndActivities() {
        log.info("Registering Temporal workflows and activities...");

        try {
            // Register trading workflows and activities
            tradingWorker.registerWorkflowImplementationTypes(
                    TradeExecutionWorkflowImpl.class,
                    BatchTradeExecutionWorkflowImpl.class
            );
            tradingWorker.registerActivitiesImplementations(tradingActivitiesImpl);
            log.info("Registered trading workflows and activities on queue: {}", TemporalConfiguration.TRADING_TASK_QUEUE);

            // Register market data workflows and activities
            marketDataWorker.registerWorkflowImplementationTypes(MarketDataPollingWorkflowImpl.class);
            marketDataWorker.registerActivitiesImplementations(tradingActivitiesImpl);
            log.info("Registered market data workflows and activities on queue: {}", TemporalConfiguration.MARKET_DATA_TASK_QUEUE);

            // Start the worker factory
            workerFactory.start();
            
            // Wait a bit to ensure workers are ready
            Thread.sleep(1000);
            
            log.info("Temporal workers started successfully. Trading worker: {}, Market data worker: {}",
                    tradingWorker.getTaskQueue(), marketDataWorker.getTaskQueue());
        } catch (Exception e) {
            log.error("Failed to register workflows and activities", e);
            throw new RuntimeException("Failed to initialize Temporal workers", e);
        }
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down Temporal workers...");
        try {
            // Shutdown gracefully
            workerFactory.shutdown();
            
            // Wait for shutdown with timeout
            workerFactory.awaitTermination(30, TimeUnit.SECONDS);
            if (!workerFactory.isTerminated()) {
                log.warn("Worker factory did not terminate within timeout, forcing shutdown");
                workerFactory.shutdownNow();
            }
            
            log.info("Temporal workers shut down successfully");
        } catch (Exception e) {
            log.error("Error during worker shutdown", e);
        }
    }
}

