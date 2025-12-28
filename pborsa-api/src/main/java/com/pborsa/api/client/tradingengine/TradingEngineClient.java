package com.pborsa.api.client.tradingengine;

import com.pborsa.api.domain.dto.market.StockTradeDto;
import com.pborsa.api.domain.dto.strategy.StrategyExecutionContext;

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
        void sendTrades(List<StockTradeDto> trades);

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
