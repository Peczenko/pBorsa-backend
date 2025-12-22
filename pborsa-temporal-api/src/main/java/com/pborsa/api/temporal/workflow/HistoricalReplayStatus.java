package com.pborsa.api.temporal.workflow;

import java.time.Instant;

/**
 * Status snapshot for historical replay workflow.
 */
public record HistoricalReplayStatus(
        String symbol,
        Instant startTime,
        Instant endTime,
        Instant currentTime,
        long emittedTicks,
        boolean running
) {
}
