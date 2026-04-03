package com.pborsa.worker.temporal.workflow;

import com.pborsa.temporal.workflow.TradeExecutionWorkflow;
import com.pborsa.domain.dto.trading.OrderResponse;
import com.pborsa.domain.dto.trading.OrderStatus;
import com.pborsa.domain.dto.trading.OrderStatusReason;
import com.pborsa.domain.dto.trading.OrderStatusUpdateRequest;
import com.pborsa.domain.dto.trading.TradeExecutionRequest;
import com.pborsa.domain.dto.trading.TradingApiOrderRequest;
import com.pborsa.temporal.activity.OrderStatusUpdateActivities;
import com.pborsa.temporal.config.TaskQueues;
import com.pborsa.temporal.activity.TradingActivities;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.failure.ActivityFailure;
import io.temporal.workflow.Workflow;
import org.slf4j.Logger;

import java.time.Duration;
import java.util.UUID;

/**
 * Implementation of the trade execution workflow.
 */
public class TradeExecutionWorkflowImpl implements TradeExecutionWorkflow {

    private static final Logger log = Workflow.getLogger(TradeExecutionWorkflowImpl.class);

    private final TradingActivities tradingActivities;
    private final OrderStatusUpdateActivities orderStatusUpdateActivities;
    public TradeExecutionWorkflowImpl() {
        // Configure retry options for trading activities (following reference project patterns)
        RetryOptions retryOptions = RetryOptions.newBuilder()
                .setInitialInterval(Duration.ofSeconds(5))
                .setMaximumInterval(Duration.ofSeconds(30))
                .setBackoffCoefficient(2.0)
                .setMaximumAttempts(3)
                .build();

        // Configure activity options with proper timeout for trading operations
        ActivityOptions activityOptions = ActivityOptions.newBuilder()
                .setStartToCloseTimeout(Duration.ofMinutes(10))
                .setTaskQueue(TaskQueues.TRADING_TASK_QUEUE)
                .setRetryOptions(retryOptions)
                .build();

        this.tradingActivities = Workflow.newActivityStub(TradingActivities.class, activityOptions);

        RetryOptions statusRetryOptions = RetryOptions.newBuilder()
                .setInitialInterval(Duration.ofSeconds(2))
                .setMaximumInterval(Duration.ofSeconds(10))
                .setBackoffCoefficient(2.0)
                .setMaximumAttempts(3)
                .build();

        ActivityOptions statusOptions = ActivityOptions.newBuilder()
                .setStartToCloseTimeout(Duration.ofMinutes(2))
                .setTaskQueue(TaskQueues.STRATEGY_EXECUTION_TASK_QUEUE)
                .setRetryOptions(statusRetryOptions)
                .build();

        this.orderStatusUpdateActivities = Workflow.newActivityStub(OrderStatusUpdateActivities.class, statusOptions);
    }

    @Override
    public OrderResponse executeTrade(TradeExecutionRequest request) {
        Long userId = request.userId();
        UUID orderId = request.orderId();
        TradingApiOrderRequest tradingApiOrderRequest = request.order();
        try {
            OrderResponse order = tradingActivities.placeOrder(userId, tradingApiOrderRequest);
            if (order != null && order.status() == OrderStatus.REJECTED) {
                updateOrderStatus(buildStatusUpdate(orderId, OrderStatus.REJECTED, order.message()));
            }
            return order;
        } catch (ActivityFailure failure) {
            String message = extractFailureMessage(failure);
            log.warn("Order placement failed for user {} orderId {}: {}", userId, orderId, message);
            updateOrderStatus(buildStatusUpdate(orderId, OrderStatus.REJECTED, message));
            return OrderResponse.buildRejectedOrderResponse(
                    tradingApiOrderRequest,
                    tradingApiOrderRequest.clientOrderId(),
                    message
            );
        }
    }

    private void updateOrderStatus(OrderStatusUpdateRequest request) {
        try {
            orderStatusUpdateActivities.updateOrderStatus(request);
        } catch (Exception e) {
            log.error("Failed to update order status for orderId {}", request != null ? request.orderId() : null, e);
        }
    }

    private String extractFailureMessage(ActivityFailure failure) {
        Throwable cause = failure.getCause();
        if (cause != null && cause.getMessage() != null && !cause.getMessage().isBlank()) {
            return cause.getMessage();
        }
        return failure.getMessage();
    }

    private OrderStatusReason resolveReason(String message) {
        if (message == null || message.isBlank()) {
            return null;
        }
        String normalized = message.toLowerCase();
        if (normalized.contains("insufficient") || normalized.contains("buying power")) {
            return OrderStatusReason.INSUFFICIENT_FUNDS;
        }
        if (normalized.contains("rate limit") || normalized.contains("429")) {
            return OrderStatusReason.RATE_LIMIT;
        }
        if (normalized.contains("market closed")) {
            return OrderStatusReason.MARKET_CLOSED;
        }
        if (normalized.contains("invalid") || normalized.contains("notional") || normalized.contains("minimal amount")) {
            return OrderStatusReason.INVALID_ORDER;
        }
        if (normalized.contains("forbidden") || normalized.contains("unauthorized")) {
            return OrderStatusReason.AUTH_ERROR;
        }
        return OrderStatusReason.UNKNOWN;
    }

    private OrderStatusUpdateRequest buildStatusUpdate(UUID orderId, OrderStatus status, String message) {
        return OrderStatusUpdateRequest.builder()
                .orderId(orderId)
                .status(status)
                .message(message)
                .reason(resolveReason(message))
                .build();
    }
}
