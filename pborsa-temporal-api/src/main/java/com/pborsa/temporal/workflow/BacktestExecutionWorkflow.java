package com.pborsa.temporal.workflow;

import com.pborsa.domain.dto.backtest.BacktestExecutionContext;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

/**
 * Workflow that orchestrates backtest execution.
 */
@WorkflowInterface
public interface BacktestExecutionWorkflow {

    @WorkflowMethod
    void execute(BacktestExecutionContext context);
}
