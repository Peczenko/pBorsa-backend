package com.pborsa.api.strategy.temporal;

import com.pborsa.api.shared.temporal.TemporalAwareService;
import com.pborsa.api.shared.config.temporal.TemporalProperties;
import com.pborsa.domain.dto.strategy.StrategyExecutionContext;
import com.pborsa.temporal.workflow.StrategyExecutionWorkflow;
import io.temporal.client.WorkflowClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.function.Function;

/**
 * Temporal integration for strategy execution workflow.
 */
@Service
@Slf4j
public class WorkflowStrategyExecutionService extends TemporalAwareService {

    private final Function<String, StrategyExecutionWorkflow> strategyExecutionWorkflowProvider;
    private final WorkflowClient workflowClient;

    public WorkflowStrategyExecutionService(TemporalProperties temporalProperties,
                                            Function<String, StrategyExecutionWorkflow> strategyExecutionWorkflowProvider,
                                            WorkflowClient workflowClient) {
        super(temporalProperties);
        this.strategyExecutionWorkflowProvider = strategyExecutionWorkflowProvider;
        this.workflowClient = workflowClient;
    }

    public String startStrategyExecution(StrategyExecutionContext context) {
        String workflowId = generateWorkflowId(context.executionId());

        runWithTemporal(() -> {
            StrategyExecutionWorkflow workflow = strategyExecutionWorkflowProvider.apply(workflowId);
            WorkflowClient.start(() -> workflow.execute(context));
            log.info("Started strategy execution workflow {}", workflowId);
        });

        return workflowId;
    }

    public void cancel(String workflowId) {
        runWithTemporal(() -> {
            log.info("Cancelling strategy execution workflow {}", workflowId);
            workflowClient.newUntypedWorkflowStub(workflowId).cancel();
        });
    }

    private String generateWorkflowId(String executionId) {
        return "strategy-exec-%s".formatted(executionId);
    }
}
