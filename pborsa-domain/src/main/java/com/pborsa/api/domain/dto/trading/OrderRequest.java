package com.pborsa.api.domain.dto.trading;

import jakarta.validation.constraints.*;
import lombok.Builder;

import java.math.BigDecimal;

/**
 * Request DTO for placing a new order.
 */
@Builder
public record OrderRequest(
        @NotBlank(message = "Symbol is required")
        @Pattern(regexp = "^[A-Z]{1,5}$", message = "Symbol must be 1-5 uppercase letters")
        String symbol,

        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be positive")
        BigDecimal quantity,

        @NotNull(message = "Order side is required")
        OrderSide side,

        @NotNull(message = "Order type is required")
        OrderType type,

        @NotNull(message = "Time in force is required")
        TimeInForce timeInForce,

        @PositiveOrZero(message = "Limit price must be non-negative")
        BigDecimal limitPrice,

        @PositiveOrZero(message = "Stop price must be non-negative")
        BigDecimal stopPrice,

        Boolean extendedHours,

        String clientOrderId
) {
    /**
     * Creates a market buy order.
     */
    public static OrderRequest marketBuy(String symbol, BigDecimal quantity) {
        return OrderRequest.builder()
                .symbol(symbol)
                .quantity(quantity)
                .side(OrderSide.BUY)
                .type(OrderType.MARKET)
                .timeInForce(TimeInForce.DAY)
                .build();
    }

    /**
     * Creates a market sell order.
     */
    public static OrderRequest marketSell(String symbol, BigDecimal quantity) {
        return OrderRequest.builder()
                .symbol(symbol)
                .quantity(quantity)
                .side(OrderSide.SELL)
                .type(OrderType.MARKET)
                .timeInForce(TimeInForce.DAY)
                .build();
    }

    /**
     * Creates a limit buy order.
     */
    public static OrderRequest limitBuy(String symbol, BigDecimal quantity, BigDecimal limitPrice) {
        return OrderRequest.builder()
                .symbol(symbol)
                .quantity(quantity)
                .side(OrderSide.BUY)
                .type(OrderType.LIMIT)
                .limitPrice(limitPrice)
                .timeInForce(TimeInForce.DAY)
                .build();
    }

    /**
     * Creates a limit sell order.
     */
    public static OrderRequest limitSell(String symbol, BigDecimal quantity, BigDecimal limitPrice) {
        return OrderRequest.builder()
                .symbol(symbol)
                .quantity(quantity)
                .side(OrderSide.SELL)
                .type(OrderType.LIMIT)
                .limitPrice(limitPrice)
                .timeInForce(TimeInForce.DAY)
                .build();
    }
}

