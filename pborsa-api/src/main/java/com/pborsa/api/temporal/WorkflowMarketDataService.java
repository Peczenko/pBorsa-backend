package com.pborsa.api.temporal;

import com.pborsa.api.config.temporal.TemporalProperties;
import com.pborsa.api.domain.dto.market.StockQuoteDto;
import com.pborsa.api.temporal.workflow.MarketDataPollingWorkflow;
import io.temporal.client.WorkflowClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * Service for market data Temporal workflows.
 * Extends TemporalAwareService for conditional execution.
 * Equivalent to WorkflowILegoDataService pattern in reference project.
 */
@Service
@Slf4j
public class WorkflowMarketDataService extends TemporalAwareService {

    private final Function<String, MarketDataPollingWorkflow> marketDataPollingWorkflowProvider;
    private final WorkflowClient workflowClient;

    public WorkflowMarketDataService(
            TemporalProperties temporalProperties,
            WorkflowClient workflowClient,
            Function<String, MarketDataPollingWorkflow> marketDataPollingWorkflowProvider) {
        super(temporalProperties);
        this.workflowClient = workflowClient;
        this.marketDataPollingWorkflowProvider = marketDataPollingWorkflowProvider;
    }

    /**
     * Starts a market data polling workflow.
     *
     * @param userId          The user ID
     * @param symbols         Symbols to poll
     * @param intervalSeconds Polling interval
     * @return The workflow ID
     */
    public String startMarketDataPolling(String userId, Set<String> symbols, int intervalSeconds) {
        log.info("Starting market data polling workflow for user {} with {} symbols", userId, symbols.size());

        runWithTemporal(() -> {
            String workflowId = generateMarketDataWorkflowId(userId);
            MarketDataPollingWorkflow workflow = marketDataPollingWorkflowProvider.apply(workflowId);
            WorkflowClient.start(workflow::startPolling, userId, symbols, intervalSeconds);
        });

        return generateMarketDataWorkflowId(userId);
    }

    /**
     * Adds symbols to an existing polling workflow.
     *
     * @param userId  The user ID
     * @param symbols Symbols to add
     */
    public void addSymbolsToPolling(String userId, Set<String> symbols) {
        runWithTemporal(() -> {
            String workflowId = generateMarketDataWorkflowId(userId);
            MarketDataPollingWorkflow workflow = workflowClient.newWorkflowStub(
                    MarketDataPollingWorkflow.class,
                    workflowId
            );
            workflow.addSymbols(symbols);
        });
    }

    /**
     * Removes symbols from an existing polling workflow.
     *
     * @param userId  The user ID
     * @param symbols Symbols to remove
     */
    public void removeSymbolsFromPolling(String userId, Set<String> symbols) {
        runWithTemporal(() -> {
            String workflowId = generateMarketDataWorkflowId(userId);
            MarketDataPollingWorkflow workflow = workflowClient.newWorkflowStub(
                    MarketDataPollingWorkflow.class,
                    workflowId
            );
            workflow.removeSymbols(symbols);
        });
    }

    /**
     * Gets the latest quotes from a polling workflow.
     *
     * @param userId The user ID
     * @return List of latest quotes
     */
    public List<StockQuoteDto> getLatestQuotesFromPolling(String userId) {
        return runWithTemporalOrElse(
                () -> {
                    String workflowId = generateMarketDataWorkflowId(userId);
                    MarketDataPollingWorkflow workflow = workflowClient.newWorkflowStub(
                            MarketDataPollingWorkflow.class,
                            workflowId
                    );
                    return workflow.getLatestQuotes();
                },
                () -> {
                    log.warn("Temporal is disabled, getLatestQuotesFromPolling returns empty list for user: {}", userId);
                    return Collections.emptyList();
                }
        );
    }

    /**
     * Stops a market data polling workflow.
     *
     * @param userId The user ID
     */
    public void stopMarketDataPolling(String userId) {
        log.info("Stopping market data polling workflow for user {}", userId);

        runWithTemporal(() -> {
            String workflowId = generateMarketDataWorkflowId(userId);
            MarketDataPollingWorkflow workflow = workflowClient.newWorkflowStub(
                    MarketDataPollingWorkflow.class,
                    workflowId
            );
            workflow.stopPolling();
        });
    }

    /**
     * Generates a workflow ID for market data polling.
     */
    private String generateMarketDataWorkflowId(String userId) {
        return "market-data-%s".formatted(userId);
    }
}
