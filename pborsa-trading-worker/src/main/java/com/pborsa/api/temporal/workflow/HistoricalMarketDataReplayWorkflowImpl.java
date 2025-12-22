package com.pborsa.api.temporal.workflow;

import io.temporal.workflow.Workflow;

import java.time.Duration;
import java.time.Instant;

/**
 * Workflow implementation that tracks historical replay progress without activities.
 */
public class HistoricalMarketDataReplayWorkflowImpl implements HistoricalMarketDataReplayWorkflow {

    private boolean running = true;
    private String symbol;
    private Instant startTime;
    private Instant endTime;
    private Instant currentTime;
    private long emittedTicks;
    private int stepSeconds;
    private long tickMillis;

    @Override
    public void startReplay(String userId,
                            String symbol,
                            Instant startTime,
                            Instant endTime,
                            int stepSeconds,
                            long tickMillis) {
        this.symbol = symbol;
        this.startTime = startTime;
        this.endTime = endTime;
        this.currentTime = startTime;
        this.stepSeconds = stepSeconds;
        this.tickMillis = tickMillis;

        while (running && !currentTime.isAfter(endTime)) {
            Workflow.sleep(Duration.ofMillis(tickMillis));
            currentTime = currentTime.plusSeconds(stepSeconds);
            emittedTicks++;
        }

        running = false;
    }

    @Override
    public void stopReplay() {
        running = false;
    }

    @Override
    public HistoricalReplayStatus getStatus() {
        return new HistoricalReplayStatus(symbol, startTime, endTime, currentTime, emittedTicks, running);
    }
}
