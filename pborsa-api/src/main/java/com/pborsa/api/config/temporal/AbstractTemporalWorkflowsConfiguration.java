package com.pborsa.api.config.temporal;

import com.pborsa.api.temporal.workflow.BatchTradeExecutionWorkflow;
import com.pborsa.api.temporal.workflow.HistoricalMarketDataReplayWorkflow;
import com.pborsa.api.temporal.workflow.MarketDataPollingWorkflow;
import com.pborsa.api.temporal.workflow.StrategyExecutionWorkflow;
import com.pborsa.api.temporal.workflow.TradeExecutionWorkflow;
import org.springframework.context.annotation.Bean;

import java.util.function.Function;

/**
 * Abstract base class for creating workflow provider beans.
 * Equivalent to AbstractImperioWorkflowsConfiguration in reference project.
 * Creates Function<String, WorkflowType> beans for each workflow type.
 */
public abstract class AbstractTemporalWorkflowsConfiguration {

    /**
     * Creates a workflow provider function for the given workflow class.
     * Concrete implementations must provide the actual provider creation logic.
     *
     * @param workflowClass The workflow interface class
     * @param <T>           The workflow type
     * @return Function that creates workflow stubs given a workflow ID
     */
    protected abstract <T> Function<String, T> createWorkflowProvider(Class<T> workflowClass);

    /**
     * Creates a provider bean for TradeExecutionWorkflow.
     */
    @Bean
    public Function<String, TradeExecutionWorkflow> tradeExecutionWorkflowProvider() {
        return createWorkflowProvider(TradeExecutionWorkflow.class);
    }

    /**
     * Creates a provider bean for BatchTradeExecutionWorkflow.
     */
    @Bean
    public Function<String, BatchTradeExecutionWorkflow> batchTradeExecutionWorkflowProvider() {
        return createWorkflowProvider(BatchTradeExecutionWorkflow.class);
    }

    /**
     * Creates a provider bean for MarketDataPollingWorkflow.
     */
    @Bean
    public Function<String, MarketDataPollingWorkflow> marketDataPollingWorkflowProvider() {
        return createWorkflowProvider(MarketDataPollingWorkflow.class);
    }

    /**
     * Creates a provider bean for HistoricalMarketDataReplayWorkflow.
     */
    @Bean
    public Function<String, HistoricalMarketDataReplayWorkflow> historicalReplayWorkflowProvider() {
        return createWorkflowProvider(HistoricalMarketDataReplayWorkflow.class);
    }

    /**
     * Creates a provider bean for StrategyExecutionWorkflow.
     */
    @Bean
    public Function<String, StrategyExecutionWorkflow> strategyExecutionWorkflowProvider() {
        return createWorkflowProvider(StrategyExecutionWorkflow.class);
    }
}
