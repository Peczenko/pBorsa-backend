package com.pborsa.api.domain.event;

import com.pborsa.api.domain.dto.trading.OrderSide;
import com.pborsa.api.domain.dto.trading.OrderStatus;
import com.pborsa.api.domain.dto.trading.OrderStatusReason;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Published when an order status is updated.
 * This event is emitted after the order and order_history are persisted.
 */
public record OrderStatusUpdatedEvent(
        UUID orderId,
        Long userId,
        OrderStatus status,
        OrderStatusReason reason,
        String message,
        String workflowId,
        String clientOrderId,
        String alpacaOrderId,
        UUID orderHistoryId,  // For resume token
        Instant updatedAt,
        Instant orderCreatedAt,
        // Fill information (available when status is FILLED or PARTIALLY_FILLED)
        String symbol,
        OrderSide side,
        BigDecimal filledQuantity,
        BigDecimal filledAvgPrice
) {
}

