package com.pborsa.api.service.strategy;

import com.pborsa.api.domain.dto.strategy.CreateUserStrategyRequest;
import com.pborsa.api.domain.dto.strategy.UpdateUserStrategyRequest;
import com.pborsa.api.domain.dto.strategy.UserStrategyDto;
import com.pborsa.api.domain.entity.BaseStrategyEntity;
import com.pborsa.api.domain.entity.UserStrategyEntity;
import com.pborsa.api.domain.entity.UserStrategyStatus;
import com.pborsa.api.domain.event.StrategyStatusChangedEvent;
import com.pborsa.api.exception.StrategyExecutionException;
import com.pborsa.api.repository.UserStrategyRepository;
import com.pborsa.api.service.mapper.UserStrategyMapper;
import com.pborsa.api.service.trading.AccountService;
import com.pborsa.api.service.trading.AssetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.pborsa.api.domain.entity.UserStrategyStatus.*;
import static com.pborsa.api.exception.StrategyExecutionException.ErrorCode.INSUFFICIENT_FUNDS;
import static com.pborsa.api.exception.StrategyExecutionException.ErrorCode.INVALID_REQUEST;

/**
 * Service for user strategy subscription operations (CRUD).
 * Publishes events on status changes to trigger side effects.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserStrategyService {

    private final UserStrategyRepository userStrategyRepository;
    private final BaseStrategyService baseStrategyService;
    private final UserStrategyMapper userStrategyMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final AccountService accountService;
    private final AssetService assetService;

    /**
     * Gets all strategies for a user.
     *
     * @param userId User ID
     * @return List of user strategy DTOs
     */
    @Transactional(readOnly = true)
    public List<UserStrategyDto> getUserStrategies(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }

        log.debug("Getting all strategies for user: {}", userId);
        List<UserStrategyEntity> strategies = userStrategyRepository.findByUserId(userId);
        return userStrategyMapper.toUserStrategyDtoList(strategies);
    }

    /**
     * Gets a specific user strategy by ID.
     *
     * @param userId     User ID
     * @param strategyId Strategy ID
     * @return Optional user strategy DTO
     */
    @Transactional(readOnly = true)
    public Optional<UserStrategyDto> getUserStrategy(Long userId, Long strategyId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        if (strategyId == null) {
            throw new IllegalArgumentException("Strategy ID is required");
        }

        log.debug("Getting strategy {} for user: {}", strategyId, userId);
        return userStrategyRepository.findByIdAndUserId(strategyId, userId)
                .map(userStrategyMapper::toUserStrategyDto);
    }

    /**
     * Creates a new user strategy subscription.
     *
     * @param userId  User ID
     * @param request Create request
     * @return Created user strategy DTO
     */
    @Transactional
    public UserStrategyDto createUserStrategy(Long userId, CreateUserStrategyRequest request) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        if (request == null) {
            throw new IllegalArgumentException("Request is required");
        }

        String code = request.baseStrategyCode().toUpperCase();
        String symbol = request.symbol().toUpperCase().trim();

        log.debug("Creating strategy for user: {}, base strategy: {}, symbol: {}", userId, code, symbol);

        // Validate base strategy exists and is active
        BaseStrategyEntity baseStrategy = baseStrategyService.getStrategyEntityByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Base strategy not found: " + code));

        if (!baseStrategy.getActive()) {
            throw new IllegalArgumentException("Base strategy is not active: " + code);
        }

        // Check for running (non-terminal) strategy with same base strategy and symbol
        if (userStrategyRepository.existsRunningStrategyByUserIdAndBaseStrategyIdAndSymbol(userId, baseStrategy.getId(), symbol)) {
            throw new StrategyExecutionException(
                    INVALID_REQUEST,
                    "You already have an active strategy with " + code + " for symbol: " + symbol +
                    ". Please stop or delete the existing strategy before creating a new one."
            );
        }

        // Validate symbol is tradeable on Alpaca
        validateSymbolTradeable(userId, symbol);

        validateBudgetSufficientBuyingPower(userId, request);

        // Create the user strategy
        UserStrategyEntity entity = new UserStrategyEntity()
                .setUserId(userId)
                .setBaseStrategy(baseStrategy)
                .setName(request.name().trim())
                .setSymbol(symbol)
                .setBudget(request.budget())
                .setStatus(UserStrategyStatus.CREATED);

        UserStrategyEntity saved = userStrategyRepository.save(entity);
        log.info("Created user strategy {} for user {} with base strategy {} and symbol {}",
                saved.getId(), userId, code, symbol);

        return userStrategyMapper.toUserStrategyDto(saved);
    }

    private void validateSymbolTradeable(Long userId, String symbol) {
        log.debug("Validating symbol {} is tradeable for user {}", symbol, userId);
        assetService.validateSymbolTradeable(userId, symbol);
    }

    private void validateBudgetSufficientBuyingPower(Long userId, CreateUserStrategyRequest request) {
        if(!accountService.hasSufficientBuyingPower(userId, request.budget())) {
            throw new StrategyExecutionException(
                    INSUFFICIENT_FUNDS,
                    "Insufficient buying power to allocate budget of " + request.budget()
            );
        }
    }

    /**
     * Activates a user strategy, transitioning from CREATED to PREPARING.
     * This triggers the data transfer workflow to the trading engine.
     *
     * @param userId     User ID
     * @param strategyId Strategy ID
     * @return Updated user strategy DTO
     * @throws IllegalStateException if strategy is not in CREATED status
     */
    @Transactional
    public Optional<UserStrategyDto> activateStrategy(Long userId, Long strategyId) {

        validateUserIdAndStrategyId(userId, strategyId);

        log.debug("Activating strategy {} for user: {}", strategyId, userId);

        Optional<UserStrategyEntity> optEntity = userStrategyRepository.findByIdAndUserId(strategyId, userId);
        if (optEntity.isEmpty()) {
            return Optional.empty();
        }

        UserStrategyEntity entity = optEntity.get();
        UserStrategyStatus oldStatus = entity.getStatus();

        // Only allow activation from CREATED status
        validateStrategyActivation(oldStatus);

        // Transition to PREPARING
        entity.setStatus(PREPARING);
        UserStrategyEntity saved = userStrategyRepository.save(entity);

        log.info("Strategy {} activated for user {} (status: {} -> PREPARING)", 
                strategyId, userId, oldStatus);

        // Publish event to trigger execution workflow
        publishStatusChangedEvent(saved, oldStatus, PREPARING);

        return Optional.of(userStrategyMapper.toUserStrategyDto(saved));
    }

    private void validateUserIdAndStrategyId(Long userId, Long strategyId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        if (strategyId == null) {
            throw new IllegalArgumentException("Strategy ID is required");
        }
    }

    private void validateStrategyActivation(UserStrategyStatus oldStatus) {
        if (!oldStatus.canTransitionTo(PREPARING)) {
            throw new IllegalStateException(
                    "Strategy can only be activated from CREATED status. Current status: " + oldStatus);
        }
    }

    /**
     * Updates an existing user strategy.
     *
     * @param userId     User ID
     * @param strategyId Strategy ID
     * @param request    Update request
     * @return Updated user strategy DTO
     */
    @Transactional
    public Optional<UserStrategyDto> updateUserStrategy(Long userId, Long strategyId, UpdateUserStrategyRequest request) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        if (strategyId == null) {
            throw new IllegalArgumentException("Strategy ID is required");
        }
        if (request == null) {
            throw new IllegalArgumentException("Request is required");
        }

        log.debug("Updating strategy {} for user: {}", strategyId, userId);

        Optional<UserStrategyEntity> optEntity = userStrategyRepository.findByIdAndUserId(strategyId, userId);
        if (optEntity.isEmpty()) {
            return Optional.empty();
        }

        UserStrategyEntity entity = optEntity.get();
        UserStrategyStatus oldStatus = entity.getStatus();

        // Update name if provided
        if (request.name() != null && !request.name().isBlank()) {
            entity.setName(request.name().trim());
        }

        // Update status if provided
        UserStrategyStatus newStatus = null;
        if (request.status() != null && !request.status().isBlank()) {
            try {
                newStatus = UserStrategyStatus.valueOf(request.status().toUpperCase());
                validateStatusTransition(oldStatus, newStatus);
                entity.setStatus(newStatus);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid status: " + request.status() + 
                        ". Valid values are: CREATED, PREPARING, ACTIVE, PAUSED, STOPPED");
            }
        }

        UserStrategyEntity saved = userStrategyRepository.save(entity);
        log.info("Updated user strategy {} for user {}", saved.getId(), userId);

        // Publish event if status changed
        if (newStatus != null && newStatus != oldStatus) {
            publishStatusChangedEvent(saved, oldStatus, newStatus);
        }

        return Optional.of(userStrategyMapper.toUserStrategyDto(saved));
    }

    /**
     * Validates that the status transition is allowed.
     *
     * @param oldStatus Current status
     * @param newStatus Target status
     * @throws StrategyExecutionException if transition is not allowed
     */
    private void validateStatusTransition(UserStrategyStatus oldStatus, UserStrategyStatus newStatus) {
        if (!oldStatus.canTransitionTo(newStatus)) {
            throw new StrategyExecutionException(
                    INVALID_REQUEST,
                    "Invalid status transition: " + oldStatus + " -> " + newStatus + 
                    ". Allowed transitions: " + ALLOWED_TRANSITIONS
            );
        }
    }

    /**
     * Publishes a strategy status changed event.
     */
    private void publishStatusChangedEvent(UserStrategyEntity entity, 
                                           UserStrategyStatus oldStatus, 
                                           UserStrategyStatus newStatus) {
        StrategyStatusChangedEvent event = new StrategyStatusChangedEvent(
                entity.getId(),
                entity.getUserId(),
                entity.getSymbol(),
                entity.getBudget(),
                oldStatus,
                newStatus
        );
        eventPublisher.publishEvent(event);
        log.debug("Published StrategyStatusChangedEvent: strategyId={}, {} -> {}", 
                entity.getId(), oldStatus, newStatus);
    }

    /**
     * Deletes a user strategy subscription.
     *
     * @param userId     User ID
     * @param strategyId Strategy ID
     * @return true if deleted, false if not found
     */
    @Transactional
    public boolean deleteUserStrategy(Long userId, Long strategyId) {

        validateUserIdAndStrategyId(userId, strategyId);

        log.debug("Deleting strategy {} for user: {}", strategyId, userId);

        Optional<UserStrategyEntity> optEntity = userStrategyRepository.findByIdAndUserId(strategyId, userId);
        if (optEntity.isEmpty()) {
            return false;
        }

        if(!strategyCanBeDeleted(optEntity.get().getStatus())) {
            throw new StrategyExecutionException(
                    INVALID_REQUEST,
                    "Strategy in status " + optEntity.get().getStatus() + " cannot be deleted."
            );
        }

        userStrategyRepository.delete(optEntity.get());
        log.info("Deleted user strategy {} for user {}", strategyId, userId);

        return true;
    }

    private boolean strategyCanBeDeleted(UserStrategyStatus status) {
        return status == CREATED || status == STOPPED || status == START_FAILED;
    }

    /**
     * Gets all active user strategies (for execution scheduling).
     *
     * @return List of active user strategies
     */
    @Transactional(readOnly = true)
    public List<UserStrategyEntity> getActiveUserStrategies() {
        return userStrategyRepository.findByStatus(UserStrategyStatus.ACTIVE);
    }

    /**
     * Marks a strategy as active after data preparation is complete.
     * Called by Temporal activity when workflow finishes.
     *
     * @param strategyId Strategy ID
     * @throws IllegalStateException if strategy is not in PREPARING status
     */
    @Transactional
    public void markStrategyActive(Long strategyId) {
        if (strategyId == null) {
            throw new IllegalArgumentException("Strategy ID is required");
        }

        log.debug("Marking strategy {} as active", strategyId);

        UserStrategyEntity entity = userStrategyRepository.findById(strategyId)
                .orElseThrow(() -> new IllegalArgumentException("Strategy not found: " + strategyId));

        UserStrategyStatus oldStatus = entity.getStatus();

        if (oldStatus != UserStrategyStatus.PREPARING) {
            log.warn("Strategy {} is not in PREPARING status (current: {}), skipping activation", 
                    strategyId, oldStatus);
            return;
        }

        entity.setStatus(UserStrategyStatus.ACTIVE);
        userStrategyRepository.save(entity);

        log.info("Strategy {} marked as active (user: {})", strategyId, entity.getUserId());

        // Publish event
        publishStatusChangedEvent(entity, oldStatus, UserStrategyStatus.ACTIVE);
    }

    /**
     * Marks a strategy as failed to start.
     * Called by Temporal workflow when data streaming or preparation fails.
     *
     * @param strategyId   Strategy ID
     * @param errorMessage Error message describing the failure
     * @throws IllegalStateException if strategy is not in PREPARING status
     */
    @Transactional
    public void markStrategyStartFailed(Long strategyId, String errorMessage) {
        if (strategyId == null) {
            throw new IllegalArgumentException("Strategy ID is required");
        }

        log.debug("Marking strategy {} as start failed: {}", strategyId, errorMessage);

        UserStrategyEntity entity = userStrategyRepository.findById(strategyId)
                .orElseThrow(() -> new IllegalArgumentException("Strategy not found: " + strategyId));

        UserStrategyStatus oldStatus = entity.getStatus();

        if (oldStatus != UserStrategyStatus.PREPARING) {
            log.warn("Strategy {} is not in PREPARING status (current: {}), skipping start failed transition",
                    strategyId, oldStatus);
            return;
        }

        entity.setStatus(UserStrategyStatus.START_FAILED);
        userStrategyRepository.save(entity);

        log.error("Strategy {} marked as start failed (user: {}): {}", strategyId, entity.getUserId(), errorMessage);

        // Publish event
        publishStatusChangedEvent(entity, oldStatus, UserStrategyStatus.START_FAILED);
    }
}


