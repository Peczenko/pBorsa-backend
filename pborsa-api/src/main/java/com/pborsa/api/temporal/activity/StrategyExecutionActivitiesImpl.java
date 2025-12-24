package com.pborsa.api.temporal.activity;

import com.pborsa.api.domain.dto.strategy.StrategyExecutionContext;
import com.pborsa.api.service.strategy.StrategyExecutionOrchestrator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Activity implementation that delegates to StrategyExecutionOrchestrator.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StrategyExecutionActivitiesImpl implements StrategyExecutionActivities {

    private final StrategyExecutionOrchestrator orchestrator;

    @Override
    public void streamHistoricalData(String executionId,
                                     String userId,
                                     String strategyId,
                                     String symbol,
                                     String timeframe,
                                     Instant start,
                                     Instant end) {
        log.info("Activity streaming data for execution {}", executionId);
        orchestrator.execute(StrategyExecutionContext.builder()
                .executionId(executionId)
                .userId(userId)
                .strategyId(strategyId)
                .symbol(symbol)
                .timeframe(timeframe)
                .start(start)
                .end(end)
                .build());
    }
}
