package com.pborsa.api.tradingengine;

import com.pborsa.domain.dto.market.StockBarDto;
import com.pborsa.domain.dto.strategy.StrategyExecutionContext;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * No-op implementation used when trading engine is disabled.
 */
@Slf4j
public class NoopTradingEngineClient implements TradingEngineClient {

    private static final TradingEngineStream NOOP_STREAM = new TradingEngineStream() {
        @Override
        public void sendBars(List<StockBarDto> bars) {
            // no-op
        }

        @Override
        public void closeStream() {
            // no-op
        }
    };

    @Override
    public TradingEngineStream startExecution(StrategyExecutionContext context) {
        log.info("Trading engine client disabled. Skipping stream for execution {}", context.executionId());
        return NOOP_STREAM;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }
}
