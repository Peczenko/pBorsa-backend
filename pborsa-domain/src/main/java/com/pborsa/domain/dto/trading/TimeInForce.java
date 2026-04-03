package com.pborsa.domain.dto.trading;

/**
 * Enum representing the time in force for an order.
 */
public enum TimeInForce {
    /**
     * Day order - valid for the current trading day.
     */
    DAY,

    /**
     * Good Till Cancelled - valid until explicitly cancelled.
     */
    GTC,

    /**
     * On Open - executed at market open.
     */
    OPG,

    /**
     * On Close - executed at market close.
     */
    CLS,

    /**
     * Immediate Or Cancel - execute immediately or cancel.
     */
    IOC,

    /**
     * Fill Or Kill - execute entirely immediately or cancel.
     */
    FOK
}

