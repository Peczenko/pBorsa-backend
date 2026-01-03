package com.pborsa.api.domain.dto.strategy;

import com.pborsa.api.domain.dto.trading.OrderStatus;
import com.pborsa.api.domain.dto.trading.OrderStatusReason;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO representing an order history entry.
 */
public record OrderHistoryDto(
        UUID id,
        UUID orderId,
        OrderStatus status,
        OrderStatusReason reason,
        String message,
        Instant createdAt
) {
}


