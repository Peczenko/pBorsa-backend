package com.pborsa.api.domain.dto.strategy;

import com.pborsa.api.domain.dto.trading.OrderSide;
import com.pborsa.api.domain.dto.trading.OrderStatus;
import com.pborsa.api.domain.dto.trading.OrderType;
import com.pborsa.api.domain.dto.trading.TimeInForce;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * DTO representing order details with strategy information.
 * Extends OrderResponse with strategyId field.
 */
public record OrderDetailDto(
        UUID id,  // Internal order ID
        String orderId,  // Alpaca order ID
        String clientOrderId,
        String symbol,
        BigDecimal quantity,
        BigDecimal filledQuantity,
        OrderSide side,
        OrderType type,
        TimeInForce timeInForce,
        BigDecimal limitPrice,
        BigDecimal stopPrice,
        BigDecimal filledAveragePrice,
        OrderStatus status,
        String message,
        Boolean extendedHours,
        Instant createdAt,
        Instant updatedAt,
        Instant submittedAt,
        Instant filledAt,
        Instant expiredAt,
        Instant cancelledAt,
        String assetClass,
        Long strategyId  // Strategy ID this order belongs to
) {
}


