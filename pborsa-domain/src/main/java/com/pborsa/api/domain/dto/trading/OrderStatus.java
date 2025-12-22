package com.pborsa.api.domain.dto.trading;

/**
 * Enum representing the status of an order.
 */
public enum OrderStatus {
    /**
     * Order is new and waiting to be processed.
     */
    NEW,

    /**
     * Order has been partially filled.
     */
    PARTIALLY_FILLED,

    /**
     * Order has been completely filled.
     */
    FILLED,

    /**
     * Order is done for the day (not filled).
     */
    DONE_FOR_DAY,

    /**
     * Order has been cancelled.
     */
    CANCELLED,

    /**
     * Order has expired.
     */
    EXPIRED,

    /**
     * Order has been replaced.
     */
    REPLACED,

    /**
     * Order is pending cancellation.
     */
    PENDING_CANCEL,

    /**
     * Order is pending replacement.
     */
    PENDING_REPLACE,

    /**
     * Order has been rejected.
     */
    REJECTED,

    /**
     * Order is pending review.
     */
    PENDING_NEW,

    /**
     * Order has been accepted.
     */
    ACCEPTED,

    /**
     * Order has been accepted for bidding.
     */
    ACCEPTED_FOR_BIDDING,

    /**
     * Order has been stopped.
     */
    STOPPED,

    /**
     * Order has been suspended.
     */
    SUSPENDED,

    /**
     * Calculated order (not submitted to market).
     */
    CALCULATED,

    /**
     * Order is held.
     */
    HELD
}

