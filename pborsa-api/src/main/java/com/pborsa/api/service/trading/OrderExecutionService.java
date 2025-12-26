package com.pborsa.api.service.trading;

import com.pborsa.api.domain.dto.trading.OrderStatus;
import com.pborsa.api.domain.dto.trading.TradingApiOrderRequest;
import com.pborsa.api.domain.entity.OrderEntity;
import com.pborsa.api.temporal.WorkflowTradingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executor;
import java.util.UUID;

@Service
@Slf4j
public class OrderExecutionService {

    private final AccountService accountService;
    private final OrderPersistenceService orderPersistenceService;
    private final WorkflowTradingService workflowTradingService;
    private final Executor tradingExecutor;
    private final TradeUpdatesStreamManager tradeUpdatesStreamManager;

    public OrderExecutionService(AccountService accountService,
                                 OrderPersistenceService orderPersistenceService,
                                 WorkflowTradingService workflowTradingService,
                                 @Qualifier("tradingExecutor") Executor tradingExecutor,
                                 TradeUpdatesStreamManager tradeUpdatesStreamManager) {
        this.accountService = accountService;
        this.orderPersistenceService = orderPersistenceService;
        this.workflowTradingService = workflowTradingService;
        this.tradingExecutor = tradingExecutor;
        this.tradeUpdatesStreamManager = tradeUpdatesStreamManager;
    }

    public OrderExecutionResult startExecution(String userId, TradingApiOrderRequest request) {
        if (!accountService.canTrade(userId)) {
            OrderEntity rejected = orderPersistenceService.createRejectedOrder(
                    userId,
                    request,
                    "Trading is not allowed for user " + userId
            );
            return OrderExecutionResult.rejected(rejected.getId().toString(), "Trading is not allowed");
        }

        String workflowId = generateTradeWorkflowId(userId);
        OrderEntity pending = orderPersistenceService.createNewOrder(userId, request, workflowId);
        tradeUpdatesStreamManager.ensureStream(userId);
        tradingExecutor.execute(() -> startWorkflow(userId, pending.getId(), request, workflowId));

        log.info("Order execution accepted for user {}, workflowId={}, orderId={}", userId, workflowId, pending.getId());
        return OrderExecutionResult.accepted(workflowId, pending.getId().toString());
    }

    private void startWorkflow(String userId, UUID orderId, TradingApiOrderRequest request, String workflowId) {
        try {
            workflowTradingService.startTradeAsync(userId, orderId, request, workflowId);
        } catch (Exception e) {
            log.error("Failed to start trade workflow for user {}, workflowId={}, orderId={}",
                    userId, workflowId, orderId, e);
            orderPersistenceService.updateStatus(orderId, OrderStatus.REJECTED, "Failed to start workflow");
        }
    }

    private String generateTradeWorkflowId(String userId) {
        return "trade-%s-%s".formatted(userId, UUID.randomUUID());
    }

    public record OrderExecutionResult(boolean accepted, String workflowId, String orderId, String message) {
        public static OrderExecutionResult accepted(String workflowId, String orderId) {
            return new OrderExecutionResult(true, workflowId, orderId, null);
        }

        public static OrderExecutionResult rejected(String orderId, String message) {
            return new OrderExecutionResult(false, null, orderId, message);
        }
    }
}
