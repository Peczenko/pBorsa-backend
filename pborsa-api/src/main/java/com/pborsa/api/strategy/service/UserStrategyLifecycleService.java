package com.pborsa.api.strategy.service;

import com.pborsa.domain.dto.strategy.UserStrategyDto;
import com.pborsa.api.strategy.entity.UserStrategyEntity;
import com.pborsa.domain.entity.UserStrategyStatus;
import com.pborsa.domain.event.StrategyStatusChangedEvent;
import com.pborsa.api.strategy.repository.UserStrategyRepository;
import com.pborsa.api.strategy.mapper.UserStrategyMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static com.pborsa.domain.entity.UserStrategyStatus.*;

/**
 * Service for user strategy lifecycle operations.
 * Handles status transitions: activate, mark active, mark start failed.
 * Publishes events on status changes to trigger side effects.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserStrategyLifecycleService {

    private final UserStrategyRepository userStrategyRepository;
    private final UserStrategyMapper userStrategyMapper;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Activates a user strategy, transitioning from CREATED to PREPARING.
     * This triggers the data transfer workflow to the trading engine.
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

        if (!oldStatus.canTransitionTo(PREPARING)) {
            throw new IllegalStateException(
                    "Strategy can only be activated from CREATED status. Current status: " + oldStatus);
        }

        entity.setStatus(PREPARING);
        UserStrategyEntity saved = userStrategyRepository.save(entity);

        log.info("Strategy {} activated for user {} (status: {} -> PREPARING)",
                strategyId, userId, oldStatus);

        publishStatusChangedEvent(saved, oldStatus, PREPARING);

        return Optional.of(userStrategyMapper.toUserStrategyDto(saved));
    }

    /**
     * Marks a strategy as active after data preparation is complete.
     * Called by Temporal activity when workflow finishes.
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

        publishStatusChangedEvent(entity, oldStatus, UserStrategyStatus.ACTIVE);
    }

    /**
     * Marks a strategy as failed to start.
     * Called by Temporal workflow when data streaming or preparation fails.
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

        publishStatusChangedEvent(entity, oldStatus, UserStrategyStatus.START_FAILED);
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
