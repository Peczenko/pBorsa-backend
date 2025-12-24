package com.pborsa.api.domain.dto.market;

import lombok.Builder;

import java.time.Instant;

/**
 * Response payload for replay control actions.
 */
@Builder
public record HistoricalReplayControlResponse(
        String replayId,
        String workflowId,
        String status,
        String symbol,
        Instant start,
        Instant end
) {
}
