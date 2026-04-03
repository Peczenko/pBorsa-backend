package com.pborsa.api.order.service;

import com.pborsa.domain.dto.trading.OrderStatus;
import com.pborsa.domain.dto.trading.OrderStatusReason;
import com.pborsa.api.order.entity.OrderEntity;
import com.pborsa.api.order.entity.OrderHistoryEntity;
import com.pborsa.domain.event.OrderStatusUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Component responsible for publishing order status update events.
 * Separated from persistence logic to follow single responsibility principle.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderStatusEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    /**
     * Publishes an OrderStatusUpdatedEvent after order history is created.
     */
    public void publishStatusUpdateEvent(OrderEntity order, OrderHistoryEntity history,
                                        OrderStatus status, String message, OrderStatusReason reason) {
        OrderStatusUpdatedEvent event = new OrderStatusUpdatedEvent(
                order.getId(),
                order.getUserId(),
                status,
                reason,
                message,
                order.getWorkflowId(),
                order.getClientOrderId(),
                order.getAlpacaOrderId(),
                history.getId(),
                history.getCreatedAt(),
                order.getCreatedAt(),
                // Fill information
                order.getSymbol(),
                order.getSide(),
                order.getFilledQuantity(),
                order.getFilledAvgPrice()
        );
        eventPublisher.publishEvent(event);
        log.debug("Published OrderStatusUpdatedEvent for orderId={} status={} filledQty={} filledAvgPrice={}",
                order.getId(), status, order.getFilledQuantity(), order.getFilledAvgPrice());
    }
}

