package com.pborsa.api.strategy.execution;

import com.pborsa.domain.dto.strategy.StrategyExecutionContext;
import com.pborsa.api.strategy.temporal.WorkflowStrategyExecutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service that starts strategy execution workflow.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StrategyExecutionService {

    private final WorkflowStrategyExecutionService workflowStrategyExecutionService;

    public void startExecution(StrategyExecutionContext context) {
        workflowStrategyExecutionService.startStrategyExecution(context);
    }
}
