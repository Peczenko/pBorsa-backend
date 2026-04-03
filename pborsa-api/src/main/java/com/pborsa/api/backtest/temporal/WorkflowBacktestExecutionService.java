package com.pborsa.api.backtest.temporal;

import com.pborsa.api.shared.temporal.TemporalAwareService;
import com.pborsa.api.shared.config.temporal.TemporalProperties;
import com.pborsa.domain.dto.backtest.BacktestExecutionContext;
import com.pborsa.temporal.workflow.BacktestExecutionWorkflow;
import io.temporal.client.WorkflowClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.function.Function;

/**
 * Temporal integration for backtest execution workflow.
 */
@Service
@Slf4j
public class WorkflowBacktestExecutionService extends TemporalAwareService {

    private final Function<String, BacktestExecutionWorkflow> backtestExecutionWorkflowProvider;
    private final WorkflowClient workflowClient;

    public WorkflowBacktestExecutionService(
            TemporalProperties temporalProperties,
            Function<String, BacktestExecutionWorkflow> backtestExecutionWorkflowProvider,
            WorkflowClient workflowClient
    ) {
        super(temporalProperties);
        this.backtestExecutionWorkflowProvider = backtestExecutionWorkflowProvider;
        this.workflowClient = workflowClient;
    }

    /**
     * Starts a backtest execution workflow.
     *
     * @param context Backtest execution context
     * @return Workflow ID
     */
    public String startBacktestExecution(BacktestExecutionContext context) {
        String workflowId = generateWorkflowId(context.backtestId());

        runWithTemporal(() -> {
            BacktestExecutionWorkflow workflow = backtestExecutionWorkflowProvider.apply(workflowId);
            WorkflowClient.start(() -> workflow.execute(context));
            log.info("Started backtest execution workflow {}", workflowId);
        });

        return workflowId;
    }

    /**
     * Cancels a running backtest workflow.
     *
     * @param workflowId Workflow ID
     */
    public void cancel(String workflowId) {
        runWithTemporal(() -> {
            log.info("Cancelling backtest execution workflow {}", workflowId);
            workflowClient.newUntypedWorkflowStub(workflowId).cancel();
        });
    }

    private String generateWorkflowId(Long backtestId) {
        return "backtest-exec-%d-%s".formatted(backtestId, currentTimestamp());
    }
}
