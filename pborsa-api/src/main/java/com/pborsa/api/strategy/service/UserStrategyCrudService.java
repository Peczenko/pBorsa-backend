package com.pborsa.api.strategy.service;

import com.pborsa.domain.dto.strategy.CreateUserStrategyRequest;
import com.pborsa.domain.dto.strategy.UpdateUserStrategyRequest;
import com.pborsa.domain.dto.strategy.UserStrategyDto;
import com.pborsa.api.strategy.entity.BaseStrategyEntity;
import com.pborsa.api.strategy.entity.UserStrategyEntity;
import com.pborsa.domain.entity.UserStrategyStatus;
import com.pborsa.domain.event.StrategyStatusChangedEvent;
import com.pborsa.domain.exception.StrategyExecutionException;
import com.pborsa.api.strategy.repository.UserStrategyRepository;
import com.pborsa.api.strategy.mapper.UserStrategyMapper;
import com.pborsa.trading.account.AccountService;
import com.pborsa.trading.account.AssetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.pborsa.domain.entity.UserStrategyStatus.*;
import static com.pborsa.domain.exception.StrategyExecutionException.ErrorCode.INSUFFICIENT_FUNDS;
import static com.pborsa.domain.exception.StrategyExecutionException.ErrorCode.INVALID_REQUEST;

/**
 * Service for user strategy CRUD operations.
 * Handles create, read, update, and delete of user strategy subscriptions.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserStrategyCrudService {

    private final UserStrategyRepository userStrategyRepository;
    private final BaseStrategyService baseStrategyService;
    private final UserStrategyMapper userStrategyMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final AccountService accountService;
    private final AssetService assetService;

    @Transactional(readOnly = true)
    public List<UserStrategyDto> getUserStrategies(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }

        log.debug("Getting all strategies for user: {}", userId);
        List<UserStrategyEntity> strategies = userStrategyRepository.findByUserId(userId);
        return userStrategyMapper.toUserStrategyDtoList(strategies);
    }

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

        BaseStrategyEntity baseStrategy = baseStrategyService.getStrategyEntityByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Base strategy not found: " + code));

        if (!baseStrategy.getActive()) {
            throw new IllegalArgumentException("Base strategy is not active: " + code);
        }

        if (userStrategyRepository.existsRunningStrategyByUserIdAndBaseStrategyIdAndSymbol(userId, baseStrategy.getId(), symbol)) {
            throw new StrategyExecutionException(
                    INVALID_REQUEST,
                    "You already have an active strategy with " + code + " for symbol: " + symbol +
                    ". Please stop or delete the existing strategy before creating a new one."
            );
        }

        validateSymbolTradeable(userId, symbol);
        validateBudgetSufficientBuyingPower(userId, request);

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

        if (request.name() != null && !request.name().isBlank()) {
            entity.setName(request.name().trim());
        }

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

        if (newStatus != null && newStatus != oldStatus) {
            publishStatusChangedEvent(saved, oldStatus, newStatus);
        }

        return Optional.of(userStrategyMapper.toUserStrategyDto(saved));
    }

    @Transactional
    public boolean deleteUserStrategy(Long userId, Long strategyId) {
        validateUserIdAndStrategyId(userId, strategyId);

        log.debug("Deleting strategy {} for user: {}", strategyId, userId);

        Optional<UserStrategyEntity> optEntity = userStrategyRepository.findByIdAndUserId(strategyId, userId);
        if (optEntity.isEmpty()) {
            return false;
        }

        if (!strategyCanBeDeleted(optEntity.get().getStatus())) {
            throw new StrategyExecutionException(
                    INVALID_REQUEST,
                    "Strategy in status " + optEntity.get().getStatus() + " cannot be deleted."
            );
        }

        userStrategyRepository.delete(optEntity.get());
        log.info("Deleted user strategy {} for user {}", strategyId, userId);

        return true;
    }

    @Transactional(readOnly = true)
    public List<UserStrategyEntity> getActiveUserStrategies() {
        return userStrategyRepository.findByStatus(UserStrategyStatus.ACTIVE);
    }

    // ==================== Private helpers ====================

    private void validateUserIdAndStrategyId(Long userId, Long strategyId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        if (strategyId == null) {
            throw new IllegalArgumentException("Strategy ID is required");
        }
    }

    private void validateSymbolTradeable(Long userId, String symbol) {
        log.debug("Validating symbol {} is tradeable for user {}", symbol, userId);
        assetService.validateSymbolTradeable(userId, symbol);
    }

    private void validateBudgetSufficientBuyingPower(Long userId, CreateUserStrategyRequest request) {
        if (!accountService.hasSufficientBuyingPower(userId, request.budget())) {
            throw new StrategyExecutionException(
                    INSUFFICIENT_FUNDS,
                    "Insufficient buying power to allocate budget of " + request.budget()
            );
        }
    }

    private void validateStatusTransition(UserStrategyStatus oldStatus, UserStrategyStatus newStatus) {
        if (!oldStatus.canTransitionTo(newStatus)) {
            throw new StrategyExecutionException(
                    INVALID_REQUEST,
                    "Invalid status transition: " + oldStatus + " -> " + newStatus +
                    ". Allowed transitions: " + ALLOWED_TRANSITIONS
            );
        }
    }

    private boolean strategyCanBeDeleted(UserStrategyStatus status) {
        return status == CREATED || status == STOPPED || status == START_FAILED;
    }

    private void publishStatusChangedEvent(UserStrategyEntity entity,
                                           UserStrategyStatus oldStatus,
                                           UserStrategyStatus newStatus) {
        StrategyStatusChangedEvent event = new StrategyStatusChangedEvent(
                entity.getId(),
                entity.getUserId(),
                entity.getSymbol(),
                entity.getBaseStrategy().getCode(),
                entity.getBudget(),
                oldStatus,
                newStatus
        );
        eventPublisher.publishEvent(event);
        log.debug("Published StrategyStatusChangedEvent: strategyId={}, {} -> {}",
                entity.getId(), oldStatus, newStatus);
    }
}
