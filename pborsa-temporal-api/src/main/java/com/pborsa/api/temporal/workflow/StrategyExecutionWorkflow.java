package com.pborsa.api.temporal.workflow;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

import java.time.Instant;

/**
 * Workflow that orchestrates strategy execution data streaming.
 */
@WorkflowInterface
public interface StrategyExecutionWorkflow {

    @WorkflowMethod
    void execute(String executionId,
                 String userId,
                 String strategyId,
                 String symbol,
                 String timeframe,
                 Instant start,
                 Instant end);
}
