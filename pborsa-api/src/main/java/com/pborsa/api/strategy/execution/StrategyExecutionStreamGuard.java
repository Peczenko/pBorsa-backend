package com.pborsa.api.strategy.execution;

import com.pborsa.api.tradingengine.TradingEngineClient;
import com.pborsa.domain.dto.market.StockBarDto;

import java.util.List;
import java.util.Objects;

/**
 * Guards the trading engine stream with health checks and heartbeat callbacks.
 * Ensures stream health before each data send operation.
 */
final class StrategyExecutionStreamGuard {

    private final TradingEngineClient.TradingEngineStream stream;
    private final Runnable heartbeat;

    StrategyExecutionStreamGuard(TradingEngineClient.TradingEngineStream stream, Runnable heartbeat) {
        this.stream = Objects.requireNonNull(stream, "stream");
        this.heartbeat = heartbeat != null ? heartbeat : () -> {};
    }

    void checkpoint() {
        heartbeat.run();
        stream.ensureHealthy();
    }

    void sendBars(List<StockBarDto> bars) {
        checkpoint();
        stream.sendBars(bars);
    }
}
