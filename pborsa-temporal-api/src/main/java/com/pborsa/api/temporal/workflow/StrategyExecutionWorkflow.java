package com.pborsa.api.temporal.workflow;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

import com.pborsa.api.domain.dto.strategy.StrategyExecutionContext;

/**
 * Workflow that orchestrates strategy execution data streaming.
 */
@WorkflowInterface
public interface StrategyExecutionWorkflow {

    @WorkflowMethod
    void execute(StrategyExecutionContext context);
}
