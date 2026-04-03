package com.pborsa.api.tradingengine;

import com.pborsa.domain.dto.market.StockBarDto;
import com.pborsa.domain.dto.strategy.StrategyExecutionContext;

import java.util.List;

/**
 * Client abstraction for streaming historical data to the trading engine.
 */
public interface TradingEngineClient {

    TradingEngineStream startExecution(StrategyExecutionContext context);

    default boolean isEnabled() {
        return true;
    }

    interface TradingEngineStream extends AutoCloseable {
        void sendBars(List<StockBarDto> bars);

        default void ensureHealthy() {
            // no-op by default
        }

        void closeStream();

        @Override
        default void close() {
            closeStream();
        }
    }
}
