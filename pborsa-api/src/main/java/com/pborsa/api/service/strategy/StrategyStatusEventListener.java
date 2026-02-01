package com.pborsa.api.service.strategy;

import com.pborsa.api.config.strategy.StrategyExecutionProperties;
import com.pborsa.api.domain.dto.strategy.StrategyExecutionContext;
import com.pborsa.api.domain.entity.UserStrategyStatus;
import com.pborsa.api.domain.event.StrategyStatusChangedEvent;
import com.pborsa.api.service.credentials.UserCredentialsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Listens for strategy status changes and triggers appropriate side effects.
 * Uses @TransactionalEventListener to ensure the event is processed only after
 * the transaction that published it has committed successfully.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StrategyStatusEventListener {

    private final StrategyExecutionService strategyExecutionService;
    private final UserCredentialsService credentialsService;
    private final StrategyExecutionProperties executionProperties;

    /**
     * Handles strategy status change events after transaction commit.
     * Routes to specific handlers based on the status transition.
     *
     * @param event The status change event
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleStrategyStatusChanged(StrategyStatusChangedEvent event) {
        log.info("Processing strategy status change: strategyId={}, userId={}, {} -> {}",
                event.strategyId(), event.userId(), event.oldStatus(), event.newStatus());

        try {
            switch (event.newStatus()) {
                case PREPARING -> handlePreparingTransition(event);
                case ACTIVE -> handleActiveTransition(event);
                case PAUSED -> handlePausedTransition(event);
                case STOPPED -> handleStoppedTransition(event);
                default -> log.debug("No handler for status transition to {}", event.newStatus());
            }
        } catch (Exception e) {
            log.error("Error handling strategy status change event: {}", event, e);
            // TODO: Consider adding retry logic or dead letter queue for failed events
        }
    }

    /**
     * Handles transition to PREPARING status.
     * This is triggered when a user activates their strategy.
     * Starts the data transfer workflow to the trading engine.
     */
    private void handlePreparingTransition(StrategyStatusChangedEvent event) {
        if (!event.oldStatus().canTransitionTo(UserStrategyStatus.PREPARING)) {
            log.warn("Unexpected transition to PREPARING from {}, expected CREATED", event.oldStatus());
            return;
        }

        log.info("Starting execution workflow for strategy {} (user: {}, symbol: {})",
                event.strategyId(), event.userId(), event.symbol());

        // Verify user has credentials before starting
        try {
            credentialsService.getCredentials(event.userId());
        } catch (Exception e) {
            log.error("User {} does not have valid credentials, cannot start strategy {}",
                    event.userId(), event.strategyId(), e);
            // TODO: Consider updating strategy status to indicate credential error
            return;
        }

        StrategyExecutionContext context = StrategyExecutionContext.create(
                event.userId(),
                event.strategyId(),
                event.symbol(),
                event.baseStrategyCode(),
                event.budget(),
                executionProperties.getLookbackPeriod(),
                executionProperties.getEndOffset()
        );

        // Start the Temporal workflow
        strategyExecutionService.startExecution(context);

        log.info("Started execution workflow {} for strategy {} (user: {}, symbol: {})",
                context.executionId(), event.strategyId(), event.userId(), event.symbol());
    }

    /**
     * Handles transition to ACTIVE status.
     * This is triggered when the data transfer workflow completes.
     */
    private void handleActiveTransition(StrategyStatusChangedEvent event) {
        if (event.oldStatus() == UserStrategyStatus.PREPARING) {
            log.info("Strategy {} is now active after data preparation (user: {})",
                    event.strategyId(), event.userId());
            // Data transfer complete, strategy is ready
            // TODO: Notify user that strategy is now active (e.g., push notification, email)
        } else if (event.oldStatus() == UserStrategyStatus.PAUSED) {
            log.info("Strategy {} resumed from paused state (user: {})",
                    event.strategyId(), event.userId());
            // TODO: Resume strategy execution logic
        }
    }

    /**
     * Handles transition to PAUSED status.
     */
    private void handlePausedTransition(StrategyStatusChangedEvent event) {
        log.info("Strategy {} paused (user: {})", event.strategyId(), event.userId());
        // TODO: Implement pause logic
        // - Stop generating new orders
        // - Optionally cancel pending orders
    }

    /**
     * Handles transition to STOPPED status.
     */
    private void handleStoppedTransition(StrategyStatusChangedEvent event) {
        log.info("Strategy {} stopped (user: {})", event.strategyId(), event.userId());
        // TODO: Implement stop logic
        // - Stop generating new orders permanently
        // - Clean up any resources
        // - Optionally close open positions
    }
}

