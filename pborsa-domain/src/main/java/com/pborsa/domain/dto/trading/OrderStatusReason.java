package com.pborsa.domain.dto.trading;

/**
 * High-level reason describing why an order status changed.
 */
public enum OrderStatusReason {
    INSUFFICIENT_FUNDS,
    RATE_LIMIT,
    INVALID_ORDER,
    MARKET_CLOSED,
    AUTH_ERROR,
    UNKNOWN
}
