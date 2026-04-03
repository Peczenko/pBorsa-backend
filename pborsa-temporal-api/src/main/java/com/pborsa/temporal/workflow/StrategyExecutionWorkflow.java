package com.pborsa.temporal.workflow;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

import com.pborsa.domain.dto.strategy.StrategyExecutionContext;

/**
 * Workflow that orchestrates strategy execution data streaming.
 */
@WorkflowInterface
public interface StrategyExecutionWorkflow {

    @WorkflowMethod
    void execute(StrategyExecutionContext context);
}
