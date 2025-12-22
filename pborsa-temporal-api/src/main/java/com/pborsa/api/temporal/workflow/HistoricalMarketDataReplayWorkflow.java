package com.pborsa.api.temporal.workflow;

import io.temporal.workflow.QueryMethod;
import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

import java.time.Instant;

/**
 * Workflow for tracking historical market data replay progress.
 */
@WorkflowInterface
public interface HistoricalMarketDataReplayWorkflow {

    @WorkflowMethod
    void startReplay(String userId,
                     String symbol,
                     Instant startTime,
                     Instant endTime,
                     int stepSeconds,
                     long tickMillis);

    @SignalMethod
    void stopReplay();

    @QueryMethod
    HistoricalReplayStatus getStatus();
}
