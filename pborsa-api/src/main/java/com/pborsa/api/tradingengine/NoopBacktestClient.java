package com.pborsa.api.tradingengine;

import com.pborsa.domain.dto.backtest.BacktestExecutionContext;
import com.pborsa.domain.dto.backtest.BacktestResultDto;
import com.pborsa.domain.dto.market.StockBarDto;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.List;

/**
 * No-op implementation used when trading engine is disabled.
 */
@Slf4j
public class NoopBacktestClient implements BacktestClient {

    @Override
    public BacktestResultDto executeBacktest(
            BacktestExecutionContext context,
            List<StockBarDto> historyBeforeStart,
            List<StockBarDto> historyTestingRange
    ) {
        log.info("Backtest client disabled. Returning empty result for backtest {}", context.backtestId());
        return BacktestResultDto.builder()
                .backtestId(context.backtestId())
                .success(false)
                .message("Trading engine is disabled")
                .orders(List.of())
                .pnl(BigDecimal.ZERO)
                .maxDrawdown(BigDecimal.ZERO)
                .totalTrades(0)
                .winningTrades(0)
                .build();
    }

    @Override
    public boolean isEnabled() {
        return false;
    }
}
