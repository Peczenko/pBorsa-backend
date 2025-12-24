package com.pborsa.api.domain.dto.market;

import lombok.Builder;

import java.time.Instant;

/**
 * Payload emitted for each historical replay tick.
 */
@Builder
public record HistoricalReplayTickDto(
        String replayId,
        String symbol,
        Instant playbackTime,
        Instant sourceBarTime,
        long sequence,
        StockBarDto bar
) {
}
