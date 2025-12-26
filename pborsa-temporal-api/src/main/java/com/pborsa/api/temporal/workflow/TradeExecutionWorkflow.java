package com.pborsa.api.temporal.workflow;

import com.pborsa.api.domain.dto.trading.TradingApiOrderRequest;
import com.pborsa.api.domain.dto.trading.OrderResponse;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

import java.util.UUID;

/**
 * Workflow interface for trade execution.
 * Handles the complete lifecycle of trade execution with proper error handling.
 */
@WorkflowInterface
public interface TradeExecutionWorkflow {

    /**
     * Executes a single trade order with validation and monitoring.
     *
     * @param userId       The user ID
     * @param tradingApiOrderRequest The order to execute
     * @return The executed order response
     */
    @WorkflowMethod
    OrderResponse executeTrade(String userId, UUID orderId, TradingApiOrderRequest tradingApiOrderRequest);
}

