package com.pborsa.worker.temporal.workflow;

import com.pborsa.temporal.workflow.BatchTradeExecutionWorkflow;
import com.pborsa.temporal.config.TaskQueues;
import com.pborsa.domain.dto.trading.TradingApiOrderRequest;
import com.pborsa.domain.dto.trading.OrderResponse;
import com.pborsa.temporal.activity.TradingActivities;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Workflow;
import org.slf4j.Logger;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class BatchTradeExecutionWorkflowImpl implements BatchTradeExecutionWorkflow {

    private static final Logger log = Workflow.getLogger(BatchTradeExecutionWorkflowImpl.class);

    private final TradingActivities tradingActivities;

    public BatchTradeExecutionWorkflowImpl() {
        // Configure retry options for batch trading activities
        RetryOptions retryOptions = RetryOptions.newBuilder()
                .setInitialInterval(Duration.ofSeconds(5))
                .setMaximumInterval(Duration.ofSeconds(30))
                .setBackoffCoefficient(2.0)
                .setMaximumAttempts(5)
                .build();

        // Configure activity options with proper timeout for batch trading operations
        ActivityOptions activityOptions = ActivityOptions.newBuilder()
                .setStartToCloseTimeout(Duration.ofMinutes(10))
                .setTaskQueue(TaskQueues.TRADING_TASK_QUEUE)
                .setRetryOptions(retryOptions)
                .build();

        this.tradingActivities = Workflow.newActivityStub(TradingActivities.class, activityOptions);
    }

    @Override
    public List<OrderResponse> executeBatchTrades(Long userId, List<TradingApiOrderRequest> orders) {
        boolean canTrade = tradingActivities.validateTradingAllowed(userId);
        if (!canTrade) {
            throw new RuntimeException("Trading is not allowed for user: " + userId);
        }

        List<OrderResponse> results = new ArrayList<>();

        for (TradingApiOrderRequest tradingApiOrderRequest : orders) {
            try {
                OrderResponse response = tradingActivities.placeOrder(userId, tradingApiOrderRequest);
                results.add(response);
            } catch (Exception e) {
                log.error("Failed to execute order for symbol {}: {}", tradingApiOrderRequest.symbol(), e.getMessage());
            }
        }

        return results;
    }
}

