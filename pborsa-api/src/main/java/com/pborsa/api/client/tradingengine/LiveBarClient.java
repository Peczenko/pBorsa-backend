package com.pborsa.api.client.tradingengine;

import com.pborsa.api.domain.dto.market.StockBarDto;

import java.util.List;
import java.util.Map;

/**
 * Client for sending live bar updates to the trading engine.
 * Separate from TradingEngineClient to maintain single responsibility.
 *
 * <p>Used by LiveBarStreamingService to push periodic bar updates
 * for active strategies.
 */
public interface LiveBarClient {

    /**
     * Sends live bar updates to the trading engine.
     *
     * @param bars       map of symbol to latest bar
     * @param timeframe  bar timeframe (e.g., "1Min")
     * @param strategyIds optional list of strategy IDs interested in these bars
     * @return result of the operation
     */
    LiveBarResult sendLiveBars(Map<String, StockBarDto> bars, String timeframe, List<Long> strategyIds);

    /**
     * Checks if the client is enabled.
     */
    default boolean isEnabled() {
        return true;
    }

    /**
     * Result of a live bar update operation.
     */
    record LiveBarResult(boolean success, String message, int barsProcessed) {
        public static LiveBarResult success(int barsProcessed) {
            return new LiveBarResult(true, "OK", barsProcessed);
        }

        public static LiveBarResult failure(String message) {
            return new LiveBarResult(false, message, 0);
        }
    }
}
