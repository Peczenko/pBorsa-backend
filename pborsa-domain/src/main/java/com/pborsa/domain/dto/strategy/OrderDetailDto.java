package com.pborsa.domain.dto.strategy;

import com.pborsa.domain.dto.trading.OrderSide;
import com.pborsa.domain.dto.trading.OrderStatus;
import com.pborsa.domain.dto.trading.OrderType;
import com.pborsa.domain.dto.trading.TimeInForce;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * DTO representing order details with user strategy information.
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
        Long userStrategyId  // User strategy ID this order belongs to
) {
}


