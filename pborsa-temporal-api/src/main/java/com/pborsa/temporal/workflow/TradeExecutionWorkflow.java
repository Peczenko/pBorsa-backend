package com.pborsa.temporal.workflow;

import com.pborsa.domain.dto.trading.TradeExecutionRequest;
import com.pborsa.domain.dto.trading.OrderResponse;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

/**
 * Workflow interface for trade execution.
 * Handles the complete lifecycle of trade execution with proper error handling.
 */
@WorkflowInterface
public interface TradeExecutionWorkflow {

    /**
     * Executes a single trade order with validation and monitoring.
     *
     * @param request Trade execution request payload
     * @return The executed order response
     */
    @WorkflowMethod
    OrderResponse executeTrade(TradeExecutionRequest request);
}

