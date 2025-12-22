package com.pborsa.api.temporal;

import com.pborsa.api.config.temporal.TemporalProperties;
import com.pborsa.api.temporal.workflow.HistoricalMarketDataReplayWorkflow;
import com.pborsa.api.temporal.workflow.HistoricalReplayStatus;
import io.temporal.client.WorkflowClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.function.Function;

/**
 * Service for historical market data replay workflows.
 */
@Service
@Slf4j
public class WorkflowHistoricalReplayService extends TemporalAwareService {

    private static final int DEFAULT_STEP_SECONDS = 5;
    private static final long DEFAULT_TICK_MILLIS = 50L;

    private final Function<String, HistoricalMarketDataReplayWorkflow> replayWorkflowProvider;
    private final WorkflowClient workflowClient;

    public WorkflowHistoricalReplayService(
            TemporalProperties temporalProperties,
            Function<String, HistoricalMarketDataReplayWorkflow> replayWorkflowProvider,
            WorkflowClient workflowClient) {
        super(temporalProperties);
        this.replayWorkflowProvider = replayWorkflowProvider;
        this.workflowClient = workflowClient;
    }

    public String startReplay(String userId,
                              String symbol,
                              Instant start,
                              Instant end,
                              Integer stepSeconds,
                              Integer tickMillis) {
        int resolvedStepSeconds = stepSeconds != null ? stepSeconds : DEFAULT_STEP_SECONDS;
        long resolvedTickMillis = tickMillis != null ? tickMillis.longValue() : DEFAULT_TICK_MILLIS;
        String workflowId = generateReplayWorkflowId(userId, symbol);

        runWithTemporal(() -> {
            HistoricalMarketDataReplayWorkflow workflow = replayWorkflowProvider.apply(workflowId);
            WorkflowClient.start(
                    workflow::startReplay,
                    userId,
                    symbol,
                    start,
                    end,
                    resolvedStepSeconds,
                    resolvedTickMillis
            );
        });

        return workflowId;
    }

    public void stopReplay(String workflowId) {
        runWithTemporal(() -> {
            HistoricalMarketDataReplayWorkflow workflow = workflowClient.newWorkflowStub(
                    HistoricalMarketDataReplayWorkflow.class,
                    workflowId
            );
            workflow.stopReplay();
        });
    }

    public HistoricalReplayStatus getStatus(String workflowId) {
        return runWithTemporalOrElse(
                () -> {
                    HistoricalMarketDataReplayWorkflow workflow = workflowClient.newWorkflowStub(
                            HistoricalMarketDataReplayWorkflow.class,
                            workflowId
                    );
                    return workflow.getStatus();
                },
                () -> new HistoricalReplayStatus(null, null, null, null, 0, false)
        );
    }

    private String generateReplayWorkflowId(String userId, String symbol) {
        return "replay-%s-%s-%s".formatted(userId, symbol, currentTimestamp());
    }
}
