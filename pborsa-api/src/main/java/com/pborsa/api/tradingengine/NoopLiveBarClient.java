package com.pborsa.api.tradingengine;

import com.pborsa.domain.dto.market.StockBarDto;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * No-op implementation of LiveBarClient for when trading engine is disabled.
 */
@Slf4j
public class NoopLiveBarClient implements LiveBarClient {

    @Override
    public LiveBarResult sendLiveBars(Map<String, StockBarDto> bars, String timeframe, List<Long> strategyIds) {
        log.debug("Live bar client disabled, skipping {} bars", bars != null ? bars.size() : 0);
        return LiveBarResult.success(0);
    }

    @Override
    public boolean isEnabled() {
        return false;
    }
}
