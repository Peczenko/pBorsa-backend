package com.pborsa.api.temporal;

import com.pborsa.api.domain.dto.market.StockQuoteDto;
import com.pborsa.api.domain.dto.trading.OrderRequest;
import com.pborsa.api.domain.dto.trading.OrderResponse;
import com.pborsa.api.temporal.service.WorkflowMarketDataService;
import com.pborsa.api.temporal.service.WorkflowTradingService;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowStub;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Facade service for interacting with Temporal workflows.
 * Delegates to domain-specific services (WorkflowTradingService, WorkflowMarketDataService).
 * Kept for backward compatibility - new code should use domain services directly.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowService {

    private final WorkflowClient workflowClient;
    private final WorkflowTradingService workflowTradingService;
    private final WorkflowMarketDataService workflowMarketDataService;

    /**
     * Executes a trade using the Temporal workflow.
     * Delegates to WorkflowTradingService.
     *
     * @param userId       The user ID
     * @param orderRequest The order request
     * @return The order response
     */
    public OrderResponse executeTrade(String userId, OrderRequest orderRequest) {
        return workflowTradingService.executeTrade(userId, orderRequest);
    }

    /**
     * Executes a trade asynchronously.
     */
    public CompletableFuture<OrderResponse> executeTradeAsync(String userId, OrderRequest orderRequest) {
        return CompletableFuture.supplyAsync(() -> executeTrade(userId, orderRequest));
    }

    /**
     * Executes multiple trades in a batch.
     * Delegates to WorkflowTradingService.
     */
    public List<OrderResponse> executeBatchTrades(String userId, List<OrderRequest> orders) {
        return workflowTradingService.executeBatchTrades(userId, orders);
    }

    /**
     * Starts a market data polling workflow.
     * Delegates to WorkflowMarketDataService.
     *
     * @param userId          The user ID
     * @param symbols         Symbols to poll
     * @param intervalSeconds Polling interval
     * @return The workflow ID
     */
    public String startMarketDataPolling(String userId, Set<String> symbols, int intervalSeconds) {
        return workflowMarketDataService.startMarketDataPolling(userId, symbols, intervalSeconds);
    }

    /**
     * Adds symbols to an existing polling workflow.
     * Delegates to WorkflowMarketDataService.
     */
    public void addSymbolsToPolling(String userId, Set<String> symbols) {
        workflowMarketDataService.addSymbolsToPolling(userId, symbols);
    }

    /**
     * Removes symbols from an existing polling workflow.
     * Delegates to WorkflowMarketDataService.
     */
    public void removeSymbolsFromPolling(String userId, Set<String> symbols) {
        workflowMarketDataService.removeSymbolsFromPolling(userId, symbols);
    }

    /**
     * Gets the latest quotes from a polling workflow.
     * Delegates to WorkflowMarketDataService.
     */
    public List<StockQuoteDto> getLatestQuotesFromPolling(String userId) {
        return workflowMarketDataService.getLatestQuotesFromPolling(userId);
    }

    /**
     * Stops a market data polling workflow.
     * Delegates to WorkflowMarketDataService.
     */
    public void stopMarketDataPolling(String userId) {
        workflowMarketDataService.stopMarketDataPolling(userId);
    }

    /**
     * Cancels any running workflow by ID.
     */
    public void cancelWorkflow(String workflowId) {
        log.info("Cancelling workflow: {}", workflowId);
        
        WorkflowStub workflow = workflowClient.newUntypedWorkflowStub(workflowId);
        workflow.cancel();
    }

    public String startTradeAsync(String userId, OrderRequest request) {
        return workflowTradingService.startTradeAsync(userId, request);
    }
}

