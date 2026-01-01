package com.pborsa.api.domain.dto.trading;

import lombok.Builder;

import java.util.UUID;

/**
 * Payload for updating order status in persistence.
 */
@Builder
public record OrderStatusUpdateRequest(
        UUID orderId,
        OrderStatus status,
        String message,
        OrderStatusReason reason
) {
}
