package com.pborsa.api.tradingengine;

import com.pborsa.api.market.temporal.WorkflowMarketDataService;
import com.pborsa.api.order.service.OrderExecutionService;
import com.pborsa.api.order.temporal.WorkflowTradingService;
import com.pborsa.domain.dto.market.StockQuoteDto;
import com.pborsa.domain.dto.trading.OrderResponse;
import com.pborsa.domain.dto.trading.TradingApiOrderRequest;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowStub;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

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
    private final OrderExecutionService orderExecutionService;

    public OrderResponse executeTrade(Long userId, UUID orderId, TradingApiOrderRequest tradingApiOrderRequest) {
        return workflowTradingService.executeTrade(userId, orderId, tradingApiOrderRequest);
    }

    public List<OrderResponse> executeBatchTrades(Long userId, List<TradingApiOrderRequest> orders) {
        return workflowTradingService.executeBatchTrades(userId, orders);
    }

    public String startMarketDataPolling(Long userId, Set<String> symbols, int intervalSeconds) {
        return workflowMarketDataService.startMarketDataPolling(userId, symbols, intervalSeconds);
    }

    public void addSymbolsToPolling(Long userId, Set<String> symbols) {
        workflowMarketDataService.addSymbolsToPolling(userId, symbols);
    }

    public void removeSymbolsFromPolling(Long userId, Set<String> symbols) {
        workflowMarketDataService.removeSymbolsFromPolling(userId, symbols);
    }

    public List<StockQuoteDto> getLatestQuotesFromPolling(Long userId) {
        return workflowMarketDataService.getLatestQuotesFromPolling(userId);
    }

    public void stopMarketDataPolling(Long userId) {
        workflowMarketDataService.stopMarketDataPolling(userId);
    }

    public void cancelWorkflow(String workflowId) {
        log.info("Cancelling workflow: {}", workflowId);
        
        WorkflowStub workflow = workflowClient.newUntypedWorkflowStub(workflowId);
        workflow.cancel();
    }

}
