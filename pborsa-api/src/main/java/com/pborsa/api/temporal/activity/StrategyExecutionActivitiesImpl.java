package com.pborsa.api.temporal.activity;

import com.pborsa.api.domain.dto.strategy.StrategyExecutionContext;
import com.pborsa.api.service.strategy.StrategyExecutionOrchestrator;
import io.temporal.activity.Activity;
import io.temporal.activity.ActivityExecutionContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Activity implementation that delegates to StrategyExecutionOrchestrator.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StrategyExecutionActivitiesImpl implements StrategyExecutionActivities {

    private final StrategyExecutionOrchestrator orchestrator;

    @Override
    public void streamHistoricalData(StrategyExecutionContext context) {
        log.info("Activity streaming data for execution {}", context.executionId());
        ActivityExecutionContext activityContext = Activity.getExecutionContext();
        orchestrator.execute(context, () -> activityContext.heartbeat(context.executionId()));
    }
}
