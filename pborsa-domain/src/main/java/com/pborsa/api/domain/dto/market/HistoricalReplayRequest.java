package com.pborsa.api.domain.dto.market;

import java.time.Instant;

/**
 * Request payload for starting a historical market data replay.
 */
public record HistoricalReplayRequest(
        String symbol,
        Instant start,
        Instant end,
        Integer stepSeconds,
        Integer tickMillis,
        String timeframe
) {
}
