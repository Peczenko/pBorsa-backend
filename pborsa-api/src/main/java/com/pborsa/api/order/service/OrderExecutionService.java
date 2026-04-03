package com.pborsa.api.order.service;

import com.pborsa.trading.account.AccountService;
import com.pborsa.domain.dto.trading.OrderExecutionRequest;
import com.pborsa.domain.dto.trading.OrderStatus;
import com.pborsa.domain.dto.trading.TradingApiOrderRequest;
import com.pborsa.api.order.entity.OrderEntity;
import com.pborsa.api.order.temporal.WorkflowTradingService;
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

    public OrderExecutionResult startExecution(OrderExecutionRequest executionRequest) {
        Long userId = executionRequest.userId();
        TradingApiOrderRequest request = executionRequest.order();
        //TODO: redefine canTrade to improve performance
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

        // Ensure WebSocket stream is connected before submitting order
        boolean streamReady = tradeUpdatesStreamManager.ensureStream(userId);
        if (!streamReady) {
            log.warn("Trade updates stream not ready for user {}, order {} will rely on reconciliation",
                    userId, pending.getId());
        }

        tradingExecutor.execute(() -> startWorkflow(userId, pending.getId(), request, workflowId));

        log.info("Order execution accepted for user {}, workflowId={}, orderId={}", userId, workflowId, pending.getId());
        return OrderExecutionResult.accepted(workflowId, pending.getId().toString());
    }

    private void startWorkflow(Long userId, UUID orderId, TradingApiOrderRequest request, String workflowId) {
        try {
            workflowTradingService.startTradeAsync(userId, orderId, request, workflowId);
        } catch (Exception e) {
            log.error("Failed to start trade workflow for user {}, workflowId={}, orderId={}",
                    userId, workflowId, orderId, e);
            orderPersistenceService.updateStatus(orderId, OrderStatus.REJECTED, "Failed to start workflow");
        }
    }

    private String generateTradeWorkflowId(Long userId) {
        return "trade-%s-%s".formatted(userId, UUID.randomUUID());
    }

}
