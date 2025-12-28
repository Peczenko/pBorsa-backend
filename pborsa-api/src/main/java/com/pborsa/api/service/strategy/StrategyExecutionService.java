package com.pborsa.api.service.strategy;

import com.pborsa.api.domain.dto.strategy.StrategyExecutionContext;
import com.pborsa.api.domain.dto.strategy.StrategyExecutionStartRequest;
import com.pborsa.api.domain.dto.strategy.StrategyExecutionStartResponse;
import com.pborsa.api.exception.StrategyExecutionException;
import com.pborsa.api.service.credentials.UserCredentialsService;
import com.pborsa.api.temporal.WorkflowStrategyExecutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;


import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Set;
import java.util.UUID;

import static java.time.ZonedDateTime.now;

/**
 * Service that validates and kicks off strategy execution.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StrategyExecutionService {

    private static final String DEFAULT_TIMEFRAME = "1Min";

    private final UserCredentialsService credentialsService;
    private final WorkflowStrategyExecutionService workflowStrategyExecutionService;

    public StrategyExecutionStartResponse startStrategyExecution(Long userId,
                                                                 String strategyId,
                                                                 StrategyExecutionStartRequest request) {
        ensureStrategyExists(strategyId);
        // Validate credentials exist
        credentialsService.getCredentials(userId);

        String symbol = request.symbol().trim().toUpperCase();
        String timeframe = StringUtils.hasText(request.timeframe())
                ? request.timeframe().trim()
                : DEFAULT_TIMEFRAME;

        ZonedDateTime nowUtc = now(java.time.ZoneOffset.UTC);
        Instant end = nowUtc.minusMinutes(15).toInstant();
        Instant start = nowUtc.minusMonths(1).toInstant();

        String executionId = UUID.randomUUID().toString();
        StrategyExecutionContext context = StrategyExecutionContext.builder()
                .executionId(executionId)
                .userId(userId)
                .strategyId(strategyId)
                .symbol(symbol)
                .timeframe(timeframe)
                .start(start)
                .end(end)
                .build();

        workflowStrategyExecutionService.startStrategyExecution(context);

        log.info("Started strategy execution {} for user {} strategy {} symbol {} timeframe {}",
                executionId, userId, strategyId, symbol, timeframe);

        return StrategyExecutionStartResponse.builder()
                .executionId(executionId)
                .userId(userId)
                .strategyId(strategyId)
                .symbol(symbol)
                .timeframe(timeframe)
                .start(start)
                .end(end)
                .status("STARTED")
                .build();
    }

    private void ensureStrategyExists(String strategyId) {
        if (!StringUtils.hasText(strategyId)) {
            throw new StrategyExecutionException(
                    StrategyExecutionException.ErrorCode.INVALID_REQUEST,
                    "Strategy ID is required");
        }
        // Placeholder strategy catalog: accept any non-empty strategy ID for now.
        log.debug("Strategy catalog lookup stubbed, accepting strategyId={}", strategyId);
    }
}
