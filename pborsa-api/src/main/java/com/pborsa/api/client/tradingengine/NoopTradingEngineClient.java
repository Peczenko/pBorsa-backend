package com.pborsa.api.client.tradingengine;

import com.pborsa.api.domain.dto.market.StockTradeDto;
import com.pborsa.api.domain.dto.strategy.StrategyExecutionContext;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * No-op implementation used when trading engine is disabled.
 */
@Slf4j
public class NoopTradingEngineClient implements TradingEngineClient {

    @Override
    public TradingEngineStream startExecution(StrategyExecutionContext context) {
        log.info("Trading engine client disabled. Skipping stream for execution {}", context.executionId());
        return new TradingEngineStream() {
            @Override
            public void sendTrades(List<StockTradeDto> trades) {
                // no-op
            }

            @Override
            public void closeStream() {
                // no-op
            }
        };
    }

    @Override
    public boolean isEnabled() {
        return false;
    }
}
