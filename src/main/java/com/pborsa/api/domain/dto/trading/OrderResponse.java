package com.pborsa.api.domain.dto.trading;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Response DTO representing an order from Alpaca.
 */
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderResponse(
        String orderId,
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
        Boolean extendedHours,
        Instant createdAt,
        Instant updatedAt,
        Instant submittedAt,
        Instant filledAt,
        Instant expiredAt,
        Instant cancelledAt,
        String assetClass
) {
    /**
     * Checks if the order is in a terminal state.
     */
    public boolean isTerminal() {
        return status == OrderStatus.FILLED
                || status == OrderStatus.CANCELLED
                || status == OrderStatus.EXPIRED
                || status == OrderStatus.REJECTED;
    }

    /**
     * Checks if the order is still active.
     */
    public boolean isActive() {
        return !isTerminal();
    }

    /**
     * Calculates the remaining quantity to be filled.
     */
    public BigDecimal remainingQuantity() {
        if (quantity == null || filledQuantity == null) {
            return BigDecimal.ZERO;
        }
        return quantity.subtract(filledQuantity);
    }

    /**
     * Calculates the fill percentage.
     */
    public BigDecimal fillPercentage() {
        if (quantity == null || filledQuantity == null || quantity.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return filledQuantity
                .divide(quantity, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }
}

