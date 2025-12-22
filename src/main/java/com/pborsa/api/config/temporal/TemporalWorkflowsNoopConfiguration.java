package com.pborsa.api.config.temporal;

import com.pborsa.api.domain.dto.market.StockQuoteDto;
import com.pborsa.api.temporal.workflow.BatchTradeExecutionWorkflow;
import com.pborsa.api.temporal.workflow.MarketDataPollingWorkflow;
import com.pborsa.api.temporal.workflow.TradeExecutionWorkflow;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * No-op implementation when Temporal is disabled.
 * Equivalent to ImperioWorkflowsNoopConfiguration in reference project.
 * Returns no-op workflow stubs that do nothing.
 */
@Configuration
@ConditionalOnProperty(name = "temporal.enabled", havingValue = "false")
@Slf4j
public class TemporalWorkflowsNoopConfiguration extends AbstractTemporalWorkflowsConfiguration {

    @Override
    @SuppressWarnings("unchecked")
    protected <T> Function<String, T> createWorkflowProvider(Class<T> workflowClass) {
        return (String workflowId) -> {
            log.debug("Creating no-op workflow stub for {} with ID: {}", workflowClass.getSimpleName(), workflowId);
            
            if (TradeExecutionWorkflow.class.isAssignableFrom(workflowClass)) {
                return (T) (TradeExecutionWorkflow) (userId, orderRequest) -> {
                    log.warn("Temporal is disabled, trade execution workflow not executed for user: {}", userId);
                    throw new UnsupportedOperationException("Temporal workflows are disabled");
                };
            } else if (BatchTradeExecutionWorkflow.class.isAssignableFrom(workflowClass)) {
                return (T) (BatchTradeExecutionWorkflow) (userId, orders) -> {
                    log.warn("Temporal is disabled, batch trade execution workflow not executed for user: {}", userId);
                    throw new UnsupportedOperationException("Temporal workflows are disabled");
                };
            } else if (MarketDataPollingWorkflow.class.isAssignableFrom(workflowClass)) {
                return (T) new MarketDataPollingWorkflow() {
                    @Override
                    public void startPolling(String userId, Set<String> symbols, int intervalSeconds) {
                        log.warn("Temporal is disabled, market data polling workflow not started for user: {}", userId);
                    }

                    @Override
                    public void addSymbols(Set<String> symbols) {
                        log.warn("Temporal is disabled, addSymbols signal ignored");
                    }

                    @Override
                    public void removeSymbols(Set<String> symbols) {
                        log.warn("Temporal is disabled, removeSymbols signal ignored");
                    }

                    @Override
                    public void stopPolling() {
                        log.warn("Temporal is disabled, stopPolling signal ignored");
                    }

                    @Override
                    public List<StockQuoteDto> getLatestQuotes() {
                        log.warn("Temporal is disabled, getLatestQuotes query returns empty list");
                        return Collections.emptyList();
                    }

                    @Override
                    public Set<String> getCurrentSymbols() {
                        log.warn("Temporal is disabled, getCurrentSymbols query returns empty set");
                        return Collections.emptySet();
                    }

                    @Override
                    public boolean isPollingActive() {
                        return false;
                    }
                };
            }
            
            throw new IllegalArgumentException("Unknown workflow type: " + workflowClass.getName());
        };
    }
}

