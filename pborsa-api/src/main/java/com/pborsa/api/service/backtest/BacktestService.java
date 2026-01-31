package com.pborsa.api.service.backtest;

import com.pborsa.api.domain.dto.backtest.BacktestDto;
import com.pborsa.api.domain.dto.backtest.CreateBacktestRequest;
import com.pborsa.api.domain.entity.BacktestEntity;
import com.pborsa.api.domain.entity.BacktestOrderEntity;
import com.pborsa.api.domain.entity.BacktestStatus;
import com.pborsa.api.domain.entity.BaseStrategyEntity;
import com.pborsa.api.domain.event.BacktestStatusChangedEvent;
import com.pborsa.api.repository.BacktestOrderRepository;
import com.pborsa.api.repository.BacktestRepository;
import com.pborsa.api.service.mapper.BacktestMapper;
import com.pborsa.api.service.strategy.BaseStrategyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service for backtest operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BacktestService {

    private final BacktestRepository backtestRepository;
    private final BacktestOrderRepository backtestOrderRepository;
    private final BaseStrategyService baseStrategyService;
    private final BacktestMapper backtestMapper;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Gets all backtests for a user.
     *
     * @param userId User ID
     * @return List of backtest DTOs
     */
    @Transactional(readOnly = true)
    public List<BacktestDto> getUserBacktests(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }

        log.debug("Getting all backtests for user: {}", userId);
        List<BacktestEntity> backtests = backtestRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return backtestMapper.toBacktestDtoList(backtests);
    }

    /**
     * Gets a specific backtest with its orders.
     *
     * @param userId     User ID
     * @param backtestId Backtest ID
     * @return Optional backtest DTO with orders
     */
    @Transactional(readOnly = true)
    public Optional<BacktestDto> getBacktest(Long userId, Long backtestId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        if (backtestId == null) {
            throw new IllegalArgumentException("Backtest ID is required");
        }

        log.debug("Getting backtest {} for user: {}", backtestId, userId);

        return backtestRepository.findByIdAndUserId(backtestId, userId)
                .map(backtest -> {
                    List<BacktestOrderEntity> orders = backtestOrderRepository
                            .findByBacktestIdOrderByExecutedAtAsc(backtestId);
                    return backtestMapper.toBacktestDto(backtest, orders);
                });
    }

    /**
     * Creates a new backtest.
     *
     * @param userId  User ID
     * @param request Create request
     * @return Created backtest DTO
     */
    @Transactional
    public BacktestDto createBacktest(Long userId, CreateBacktestRequest request) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        if (request == null) {
            throw new IllegalArgumentException("Request is required");
        }

        String code = request.baseStrategyCode().toUpperCase();
        String symbol = request.symbol().toUpperCase().trim();

        log.debug("Creating backtest for user: {}, base strategy: {}, symbol: {}", userId, code, symbol);

        // Validate base strategy exists and is active
        BaseStrategyEntity baseStrategy = baseStrategyService.getStrategyEntityByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Base strategy not found: " + code));

        if (!baseStrategy.getActive()) {
            throw new IllegalArgumentException("Base strategy is not active: " + code);
        }

        // Validate time range
        if (request.testingStart().isAfter(request.testingEnd())) {
            throw new IllegalArgumentException("Testing start must be before testing end");
        }

        // Create the backtest
        BacktestEntity entity = new BacktestEntity()
                .setUserId(userId)
                .setBaseStrategy(baseStrategy)
                .setName(request.name().trim())
                .setSymbol(symbol)
                .setBudget(request.budget())
                .setTestingStart(request.testingStart())
                .setTestingEnd(request.testingEnd())
                .setStatus(BacktestStatus.CREATED);

        BacktestEntity saved = backtestRepository.save(entity);
        log.info("Created backtest {} for user {} with base strategy {} and symbol {}",
                saved.getId(), userId, code, symbol);

        return backtestMapper.toBacktestDto(saved);
    }

    /**
     * Starts a backtest execution.
     *
     * @param userId     User ID
     * @param backtestId Backtest ID
     * @return Updated backtest DTO
     * @throws IllegalStateException if backtest is not in CREATED status
     */
    @Transactional
    public Optional<BacktestDto> startBacktest(Long userId, Long backtestId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        if (backtestId == null) {
            throw new IllegalArgumentException("Backtest ID is required");
        }

        log.debug("Starting backtest {} for user: {}", backtestId, userId);

        Optional<BacktestEntity> optEntity = backtestRepository.findByIdAndUserId(backtestId, userId);
        if (optEntity.isEmpty()) {
            return Optional.empty();
        }

        BacktestEntity entity = optEntity.get();
        BacktestStatus oldStatus = entity.getStatus();

        // Only allow starting from CREATED status
        if (!oldStatus.canTransitionTo(BacktestStatus.PREPARING)) {
            throw new IllegalStateException(
                    "Backtest can only be started from CREATED status. Current status: " + oldStatus);
        }

        // Transition to PREPARING
        entity.setStatus(BacktestStatus.PREPARING);
        BacktestEntity saved = backtestRepository.save(entity);

        log.info("Backtest {} started for user {} (status: {} -> PREPARING)",
                backtestId, userId, oldStatus);

        // Publish event to trigger execution workflow
        publishStatusChangedEvent(saved, oldStatus, BacktestStatus.PREPARING);

        return Optional.of(backtestMapper.toBacktestDto(saved));
    }

    /**
     * Deletes a backtest.
     *
     * @param userId     User ID
     * @param backtestId Backtest ID
     * @return true if deleted, false if not found
     */
    @Transactional
    public boolean deleteBacktest(Long userId, Long backtestId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        if (backtestId == null) {
            throw new IllegalArgumentException("Backtest ID is required");
        }

        log.debug("Deleting backtest {} for user: {}", backtestId, userId);

        Optional<BacktestEntity> optEntity = backtestRepository.findByIdAndUserId(backtestId, userId);
        if (optEntity.isEmpty()) {
            return false;
        }

        BacktestEntity entity = optEntity.get();

        // Only allow deletion of terminal or initial states
        if (!canBeDeleted(entity.getStatus())) {
            throw new IllegalStateException(
                    "Backtest in status " + entity.getStatus() + " cannot be deleted.");
        }

        backtestRepository.delete(entity);
        log.info("Deleted backtest {} for user {}", backtestId, userId);

        return true;
    }

    private boolean canBeDeleted(BacktestStatus status) {
        return status == BacktestStatus.CREATED ||
                status == BacktestStatus.COMPLETED ||
                status == BacktestStatus.FAILED;
    }

    private void publishStatusChangedEvent(BacktestEntity entity,
                                           BacktestStatus oldStatus,
                                           BacktestStatus newStatus) {
        BacktestStatusChangedEvent event = new BacktestStatusChangedEvent(
                entity.getId(),
                entity.getUserId(),
                entity.getBaseStrategy().getId(),
                entity.getBaseStrategy().getCode(),
                entity.getSymbol(),
                entity.getBudget(),
                entity.getTestingStart(),
                entity.getTestingEnd(),
                oldStatus,
                newStatus
        );
        eventPublisher.publishEvent(event);
        log.debug("Published BacktestStatusChangedEvent: backtestId={}, {} -> {}",
                entity.getId(), oldStatus, newStatus);
    }
}
