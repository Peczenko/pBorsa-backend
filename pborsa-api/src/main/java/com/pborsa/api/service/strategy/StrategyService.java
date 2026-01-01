package com.pborsa.api.service.strategy;

import com.pborsa.api.domain.dto.strategy.StrategyExecutionContext;
import com.pborsa.api.domain.dto.strategy.StrategyExecutionStartRequest;
import com.pborsa.api.domain.dto.strategy.StrategyExecutionStartResponse;
import com.pborsa.api.exception.StrategyExecutionException;
import com.pborsa.api.repository.StrategyRepository;
import com.pborsa.api.service.credentials.UserCredentialsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Orchestrates strategy operations invoked by controllers.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StrategyService {

    private final UserCredentialsService credentialsService;
    private final StrategyExecutionService strategyExecutionService;
    private final StrategyRepository strategyRepository;

    public StrategyExecutionStartResponse startStrategy(Long userId,
                                                        Long strategyId,
                                                        StrategyExecutionStartRequest request) {
        ensureStrategyExists(strategyId);
        credentialsService.getCredentials(userId);

        String symbol = request.symbol().trim().toUpperCase();

        ZonedDateTime nowUtc = ZonedDateTime.now(java.time.ZoneOffset.UTC);
        Instant end = nowUtc.minusMinutes(15).toInstant();
        Instant start = nowUtc.minusMonths(3).toInstant();

        String executionId = UUID.randomUUID().toString();
        StrategyExecutionContext context = StrategyExecutionContext.builder()
                .executionId(executionId)
                .userId(userId)
                .strategyId(strategyId)
                .symbol(symbol)
                .start(start)
                .end(end)
                .build();

        strategyExecutionService.startExecution(context);

        log.info("StrategyService started execution {} for user {} strategy {} symbol {}",
                executionId, userId, strategyId, symbol);

        return StrategyExecutionStartResponse.builder()
                .executionId(executionId)
                .userId(userId)
                .strategyId(strategyId)
                .symbol(symbol)
                .start(start)
                .end(end)
                .status("STARTED")
                .build();
    }

    private void ensureStrategyExists(Long strategyId) {
        if (strategyId == null) {
            throw new StrategyExecutionException(
                    StrategyExecutionException.ErrorCode.INVALID_REQUEST,
                    "Strategy ID is required");
        }
        if (!strategyRepository.existsById(strategyId)) {
            throw new StrategyExecutionException(
                    StrategyExecutionException.ErrorCode.INVALID_REQUEST,
                    "Strategy not found: " + strategyId);
        }
    }
}
