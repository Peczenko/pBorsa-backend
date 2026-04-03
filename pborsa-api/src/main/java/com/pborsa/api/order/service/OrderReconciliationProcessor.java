package com.pborsa.api.order.service;

import com.pborsa.trading.account.OrderService;
import com.pborsa.domain.dto.trading.OrderResponse;
import com.pborsa.api.order.entity.OrderEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderReconciliationProcessor {

    private static final String MESSAGE_STATUS_CHANGE = "reconcile_status_change";

    private final OrderService orderService;
    private final OrderPersistenceService orderPersistenceService;

    public void reconcileUser(Long userId,
                              List<OrderEntity> orders,
                              Instant end) {
        if (orders.isEmpty()) {
            return;
        }

        List<OrderResponse> openOrders = orderService.getOpenOrders(userId);
        List<OrderResponse> closedOrders = orderService.getOrders(
                userId,
                "closed",
                500,
                null,
                ZonedDateTime.ofInstant(end, ZoneOffset.UTC),
                false
        );

        Map<String, OrderResponse> byExternalId = new HashMap<>();
        indexOrders(byExternalId, openOrders);
        indexOrders(byExternalId, closedOrders);

        for (OrderEntity entity : orders) {
            OrderResponse remote = findRemoteOrder(byExternalId, entity);
            if (remote == null || remote.status() == null) {
                continue;
            }

            // Check if status changed or fill data needs updating
            boolean statusChanged = remote.status() != entity.getStatus();
            boolean needsFillDataUpdate = needsFillDataUpdate(entity, remote);

            if (statusChanged || needsFillDataUpdate) {
                orderPersistenceService.updateStatusByExternalIds(
                        remote.orderId(),
                        remote.clientOrderId(),
                        remote.status(),
                        MESSAGE_STATUS_CHANGE,
                        null,  // reason
                        remote // pass fill data from remote order
                );
            }
        }
    }

    private boolean needsFillDataUpdate(OrderEntity entity, OrderResponse remote) {
        // Update if we have new fill data from Alpaca that we don't have locally
        if (remote.filledQuantity() != null && entity.getFilledQuantity() == null) {
            return true;
        }
        if (remote.filledAveragePrice() != null && entity.getFilledAvgPrice() == null) {
            return true;
        }
        // Also update if remote fill data is different (e.g., partial fill progression)
        return remote.filledQuantity() != null && remote.filledQuantity().compareTo(entity.getFilledQuantity()) != 0;
    }

    private void indexOrders(Map<String, OrderResponse> target, List<OrderResponse> orders) {
        for (OrderResponse order : orders) {
            if (order.orderId() != null && !order.orderId().isBlank()) {
                target.put(order.orderId(), order);
            }
            if (order.clientOrderId() != null && !order.clientOrderId().isBlank()) {
                target.put(order.clientOrderId(), order);
            }
        }
    }

    private OrderResponse findRemoteOrder(Map<String, OrderResponse> byExternalId, OrderEntity entity) {
        if (entity.getAlpacaOrderId() != null && !entity.getAlpacaOrderId().isBlank()) {
            OrderResponse byAlpaca = byExternalId.get(entity.getAlpacaOrderId());
            if (byAlpaca != null) {
                return byAlpaca;
            }
        }
        if (entity.getClientOrderId() != null && !entity.getClientOrderId().isBlank()) {
            return byExternalId.get(entity.getClientOrderId());
        }
        return null;
    }
}
