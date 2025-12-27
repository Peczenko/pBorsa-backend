package com.pborsa.api.temporal;

import com.pborsa.api.config.temporal.TemporalProperties;
import com.pborsa.api.domain.dto.trading.TradingApiOrderRequest;
import com.pborsa.api.domain.dto.trading.OrderResponse;
import com.pborsa.api.temporal.workflow.BatchTradeExecutionWorkflow;
import com.pborsa.api.temporal.workflow.TradeExecutionWorkflow;
import io.temporal.client.WorkflowClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

/**
 * Service for trading-related Temporal workflows.
 * Extends TemporalAwareService for conditional execution.
 * Equivalent to WorkflowEmailService pattern in reference project.
 */
@Service
@Slf4j
public class WorkflowTradingService extends TemporalAwareService {

    private final Function<String, TradeExecutionWorkflow> tradeExecutionWorkflowProvider;
    private final Function<String, BatchTradeExecutionWorkflow> batchTradeExecutionWorkflowProvider;
    public WorkflowTradingService(
            TemporalProperties temporalProperties,
            Function<String, TradeExecutionWorkflow> tradeExecutionWorkflowProvider,
            Function<String, BatchTradeExecutionWorkflow> batchTradeExecutionWorkflowProvider) {
        super(temporalProperties);
        this.tradeExecutionWorkflowProvider = tradeExecutionWorkflowProvider;
        this.batchTradeExecutionWorkflowProvider = batchTradeExecutionWorkflowProvider;
    }

    /**
     * Executes a trade using the Temporal workflow.
     *
     * @param userId                 The user ID
     * @param tradingApiOrderRequest The order request
     * @return The order response
     */
    public OrderResponse executeTrade(Long userId, UUID orderId, TradingApiOrderRequest tradingApiOrderRequest) {
        log.info("Starting trade execution workflow for user {} symbol {}", userId, tradingApiOrderRequest.symbol());

        return runWithTemporalOrElse(
                () -> {
                    String workflowId = generateTradeWorkflowId(userId);
                    TradeExecutionWorkflow workflow = tradeExecutionWorkflowProvider.apply(workflowId);
                    return workflow.executeTrade(userId, orderId, tradingApiOrderRequest);
                },
                () -> {
                    log.warn("Temporal is disabled, trade execution workflow not executed for user: {}", userId);
                    throw new UnsupportedOperationException("Temporal workflows are disabled");
                }
        );
    }

    /**
     * Executes multiple trades in a batch using the Temporal workflow.
     *
     * @param userId The user ID
     * @param orders List of orders to execute
     * @return List of order responses
     */
    public List<OrderResponse> executeBatchTrades(Long userId, List<TradingApiOrderRequest> orders) {
        log.info("Starting batch trade execution workflow for user {} with {} orders", userId, orders.size());

        return runWithTemporalOrElse(
                () -> {
                    String workflowId = generateBatchTradeWorkflowId(userId);
                    BatchTradeExecutionWorkflow workflow = batchTradeExecutionWorkflowProvider.apply(workflowId);
                    return workflow.executeBatchTrades(userId, orders);
                },
                () -> {
                    log.warn("Temporal is disabled, batch trade execution workflow not executed for user: {}", userId);
                    throw new UnsupportedOperationException("Temporal workflows are disabled");
                }
        );
    }

    /**
     * Generates a workflow ID for trade execution.
     */
    private String generateTradeWorkflowId(Long userId) {
        return "trade-%s-%s".formatted(userId, UUID.randomUUID());
    }

    /**
     * Generates a workflow ID for batch trade execution.
     */
    private String generateBatchTradeWorkflowId(Long userId) {
        return "batch-trade-%s-%s".formatted(userId, UUID.randomUUID());
    }

    public void startTradeAsync(Long userId, UUID orderId, TradingApiOrderRequest request, String workflowId) {
        runWithTemporalOrElse(
                () -> {
                    TradeExecutionWorkflow wf = tradeExecutionWorkflowProvider.apply(workflowId);
                    WorkflowClient.start(wf::executeTrade, userId, orderId, request);
                },
                () -> {
                    log.warn("Temporal is disabled, trade execution workflow not executed for user: {}", userId);
                    throw new UnsupportedOperationException("Temporal workflows are disabled");
                }
        );
    }
}
