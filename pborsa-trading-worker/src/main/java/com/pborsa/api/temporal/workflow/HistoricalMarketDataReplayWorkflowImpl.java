package com.pborsa.api.temporal.workflow;

import io.temporal.workflow.Workflow;

import java.time.Instant;

/**
 * Workflow implementation that tracks historical replay progress without activities.
 */
public class HistoricalMarketDataReplayWorkflowImpl implements HistoricalMarketDataReplayWorkflow {

    private boolean running;
    private String symbol;
    private Instant startTime;
    private Instant endTime;

    @Override
    public void startReplay(Long userId,
                            String symbol,
                            Instant startTime,
                            Instant endTime,
                            int stepSeconds) {
        this.symbol = symbol;
        this.startTime = startTime;
        this.endTime = endTime;
        this.running = true;

        Workflow.await(() -> !running);
    }

    @Override
    public void stopReplay() {
        running = false;
    }

    @Override
    public HistoricalReplayStatus getStatus() {
        Instant workflowTime = Instant.ofEpochMilli(Workflow.currentTimeMillis());
        return new HistoricalReplayStatus(symbol, startTime, endTime, workflowTime, 0, running);
    }
}
