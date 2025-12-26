package com.pborsa.api.service.trading;

import com.pborsa.api.domain.dto.trading.OrderResponse;
import com.pborsa.api.domain.dto.trading.OrderStatus;
import com.pborsa.api.domain.entity.OrderEntity;
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
    private static final String MESSAGE_NO_CHANGE_CLOSE = "reconcile_no_change_close";

    private final OrderService orderService;
    private final OrderPersistenceService orderPersistenceService;

    public void reconcileUser(String userId,
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

            if (remote.status() != entity.getStatus()) {
                orderPersistenceService.updateStatusByExternalIds(
                        remote.orderId(),
                        remote.clientOrderId(),
                        remote.status(),
                        MESSAGE_STATUS_CHANGE
                );
                continue;
            }

            if (shouldClose(remote.status())) {
                closeRemoteOrder(userId, remote);
            }
        }
    }

    private void closeRemoteOrder(String userId, OrderResponse remote) {
        if (remote.orderId() == null || remote.orderId().isBlank()) {
            log.debug("Reconcile close skipped, missing alpaca order id for user {}", userId);
            return;
        }
        try {
            boolean cancelled = orderService.cancelOrder(userId, remote.orderId());
            if (cancelled) {
                orderPersistenceService.updateStatusByExternalIds(
                        remote.orderId(),
                        remote.clientOrderId(),
                        OrderStatus.CANCELED,
                        MESSAGE_NO_CHANGE_CLOSE
                );
                log.info("Reconcile closed order user={} alpacaOrderId={}", userId, remote.orderId());
            }
        } catch (Exception e) {
            log.warn("Failed to close stale order user={} alpacaOrderId={}", userId, remote.orderId(), e);
        }
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

    private boolean shouldClose(OrderStatus status) {
        return status != null && switch (status) {
            case FILLED, CANCELED, EXPIRED, REJECTED, DONE_FOR_DAY -> false;
            default -> true;
        };
    }
}
