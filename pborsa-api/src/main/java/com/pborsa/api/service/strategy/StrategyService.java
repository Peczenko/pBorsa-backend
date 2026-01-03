package com.pborsa.api.service.strategy;

import com.pborsa.api.domain.dto.strategy.StrategyDto;
import com.pborsa.api.domain.dto.strategy.StrategyExecutionContext;
import com.pborsa.api.domain.dto.strategy.StrategyExecutionStartRequest;
import com.pborsa.api.domain.dto.strategy.StrategyExecutionStartResponse;
import com.pborsa.api.exception.StrategyExecutionException;
import com.pborsa.api.repository.StrategyRepository;
import com.pborsa.api.service.credentials.UserCredentialsService;
import com.pborsa.api.service.mapper.StrategyMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
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
    private final StrategyPersistenceService strategyPersistenceService;
    private final StrategyMapper strategyMapper;

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

    /**
     * Gets all strategies for a user.
     * Security: Validates userId is not null.
     * Note: Currently strategies are global, so this returns all strategies.
     * In the future, if strategies become user-specific, this should filter by userId.
     *
     * @param userId User ID (validated but not used for filtering currently)
     * @return List of strategy DTOs
     */
    public List<StrategyDto> getStrategiesByUserId(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }

        log.debug("Getting all strategies for user: {}", userId);
        // Currently strategies are global, so we return all strategies
        // In the future, if strategies become user-specific, filter by userId
        return strategyMapper.toStrategyDtoList(strategyPersistenceService.findAllStrategies());
    }

    /**
     * Gets a specific strategy by ID.
     * Security: Validates userId is not null.
     *
     * @param userId     User ID (validated but not used for filtering currently)
     * @param strategyId Strategy ID
     * @return Optional strategy DTO
     */
    public Optional<StrategyDto> getStrategyById(Long userId, Long strategyId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        if (strategyId == null) {
            throw new IllegalArgumentException("Strategy ID is required");
        }

        log.debug("Getting strategy {} for user: {}", strategyId, userId);
        return strategyPersistenceService.findStrategyById(strategyId)
                .map(strategyMapper::toStrategyDto);
    }
}
