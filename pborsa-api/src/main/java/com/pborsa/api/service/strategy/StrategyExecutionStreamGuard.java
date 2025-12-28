package com.pborsa.api.service.strategy;

import com.pborsa.api.client.tradingengine.TradingEngineClient;
import com.pborsa.api.domain.dto.market.StockTradeDto;

import java.util.List;
import java.util.Objects;

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

    void sendTrades(List<StockTradeDto> trades) {
        checkpoint();
        stream.sendTrades(trades);
    }
}
