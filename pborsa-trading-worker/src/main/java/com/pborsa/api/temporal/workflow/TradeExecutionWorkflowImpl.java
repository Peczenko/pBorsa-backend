package com.pborsa.api.temporal.workflow;

import com.pborsa.api.domain.dto.trading.TradingApiOrderRequest;
import com.pborsa.api.domain.dto.trading.OrderResponse;
import com.pborsa.api.domain.dto.trading.OrderStatus;
import com.pborsa.api.temporal.activity.OrderStatusUpdateActivity;
import com.pborsa.api.temporal.config.TaskQueues;
import com.pborsa.api.temporal.activity.TradingActivities;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
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
    private final OrderStatusUpdateActivity orderStatusUpdateActivity;

    public TradeExecutionWorkflowImpl() {
        // Configure retry options for trading activities (following reference project patterns)
        RetryOptions retryOptions = RetryOptions.newBuilder()
                .setInitialInterval(Duration.ofSeconds(5))
                .setMaximumInterval(Duration.ofSeconds(30))
                .setBackoffCoefficient(2.0)
                .setMaximumAttempts(5)
                .build();

        // Configure activity options with proper timeout for trading operations
        ActivityOptions activityOptions = ActivityOptions.newBuilder()
                .setStartToCloseTimeout(Duration.ofMinutes(10))
                .setTaskQueue(TaskQueues.TRADING_TASK_QUEUE)
                .setRetryOptions(retryOptions)
                .build();

        ActivityOptions statusOptions = ActivityOptions.newBuilder()
                .setStartToCloseTimeout(Duration.ofSeconds(30))
                .setTaskQueue(TaskQueues.ORDER_STATUS_TASK_QUEUE)
                .setRetryOptions(retryOptions)
                .build();

        this.tradingActivities = Workflow.newActivityStub(TradingActivities.class, activityOptions);
        this.orderStatusUpdateActivity = Workflow.newActivityStub(OrderStatusUpdateActivity.class, statusOptions);
    }

    @Override
    public OrderResponse executeTrade(String userId, UUID orderId, TradingApiOrderRequest tradingApiOrderRequest) {
        // Step 1: Place the order
        OrderResponse order = tradingActivities.placeOrder(userId, tradingApiOrderRequest);

        // Step 2: Update order status to PLACED
        orderStatusUpdateActivity.updateOrderStatus(orderId, order.status(), order.message());

        return order;
    }

    private boolean isTerminalStatus(OrderStatus status) {
        return status == OrderStatus.FILLED
                || status == OrderStatus.CANCELLED
                || status == OrderStatus.EXPIRED
                || status == OrderStatus.REJECTED;
    }
}
