package com.pborsa.api.temporal.workflow;

import com.pborsa.api.domain.dto.trading.TradingApiOrderRequest;
import com.pborsa.api.domain.dto.trading.OrderResponse;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

import java.util.List;

/**
 * Workflow interface for batch trade execution.
 * Handles multiple trade orders in a single workflow execution.
 */
@WorkflowInterface
public interface BatchTradeExecutionWorkflow {

    /**
     * Executes multiple trade orders.
     *
     * @param userId The user ID
     * @param orders List of orders to execute
     * @return List of executed order responses
     */
    @WorkflowMethod
    List<OrderResponse> executeBatchTrades(Long userId, List<TradingApiOrderRequest> orders);
}

