package com.pborsa.api.strategy.temporal.activity;

import com.pborsa.temporal.activity.StrategyExecutionActivities;
import com.pborsa.domain.dto.strategy.StrategyExecutionContext;
import com.pborsa.api.strategy.execution.StrategyExecutionOrchestrator;
import com.pborsa.api.strategy.service.UserStrategyLifecycleService;
import io.temporal.activity.Activity;
import io.temporal.activity.ActivityExecutionContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Activity implementation that delegates to StrategyExecutionOrchestrator
 * and handles strategy status updates.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StrategyExecutionActivitiesImpl implements StrategyExecutionActivities {

    private final StrategyExecutionOrchestrator orchestrator;
    private final UserStrategyLifecycleService userStrategyLifecycleService;

    @Override
    public void streamHistoricalData(StrategyExecutionContext context) {
        log.info("Activity streaming data for execution {}", context.executionId());
        ActivityExecutionContext activityContext = Activity.getExecutionContext();
        orchestrator.execute(context, () -> activityContext.heartbeat(context.executionId()));
    }

    @Override
    public void markStrategyActive(Long strategyId) {
        log.info("Activity marking strategy {} as active", strategyId);
        userStrategyLifecycleService.markStrategyActive(strategyId);
        log.info("Strategy {} marked as active", strategyId);
    }

    @Override
    public void markStrategyStartFailed(Long strategyId, String errorMessage) {
        log.info("Activity marking strategy {} as start failed: {}", strategyId, errorMessage);
        userStrategyLifecycleService.markStrategyStartFailed(strategyId, errorMessage);
        log.info("Strategy {} marked as start failed", strategyId);
    }
}
