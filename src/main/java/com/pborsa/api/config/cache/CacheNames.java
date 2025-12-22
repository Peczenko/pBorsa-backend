package com.pborsa.api.config.cache;

/**
 * Constants for cache names used throughout the application.
 * Centralizes cache naming to avoid magic strings.
 */
public final class CacheNames {

    private CacheNames() {
        // Utility class - prevent instantiation
    }

    /**
     * Cache for user API credentials (Alpaca keys).
     */
    public static final String API_CREDENTIALS = "apiCredentials";

    /**
     * Cache for Alpaca API client instances.
     */
    public static final String ALPACA_CLIENTS = "alpacaClients";

    /**
     * Cache for real-time market data.
     */
    public static final String MARKET_DATA = "marketData";

    /**
     * Cache for user positions.
     */
    public static final String POSITIONS = "positions";

    /**
     * Cache for account information.
     */
    public static final String ACCOUNT_INFO = "accountInfo";

    /**
     * Cache for orders.
     */
    public static final String ORDERS = "orders";

    /**
     * Cache for stock quotes.
     */
    public static final String QUOTES = "quotes";

    /**
     * Cache for latest trades.
     */
    public static final String TRADES = "trades";

    /**
     * Cache for OHLCV bars.
     */
    public static final String BARS = "bars";
}

