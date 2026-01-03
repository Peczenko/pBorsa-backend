package com.pborsa.api.service.trading;

import com.pborsa.api.domain.dto.trading.OrderStatus;
import com.pborsa.api.domain.dto.trading.OrderStatusReason;
import com.pborsa.api.domain.entity.OrderEntity;
import com.pborsa.api.domain.entity.OrderHistoryEntity;
import com.pborsa.api.domain.event.OrderStatusUpdatedEvent;
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
                order.getCreatedAt()
        );
        eventPublisher.publishEvent(event);
        log.debug("Published OrderStatusUpdatedEvent for orderId={} status={}", order.getId(), status);
    }
}

