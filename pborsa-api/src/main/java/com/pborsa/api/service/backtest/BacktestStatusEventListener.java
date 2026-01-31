package com.pborsa.api.service.backtest;

import com.pborsa.api.domain.dto.backtest.BacktestExecutionContext;
import com.pborsa.api.domain.entity.BacktestStatus;
import com.pborsa.api.domain.event.BacktestStatusChangedEvent;
import com.pborsa.api.service.credentials.UserCredentialsService;
import com.pborsa.api.temporal.WorkflowBacktestExecutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Listens for backtest status changes and triggers appropriate side effects.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BacktestStatusEventListener {

    private final WorkflowBacktestExecutionService workflowBacktestExecutionService;
    private final UserCredentialsService credentialsService;

    /**
     * Handles backtest status change events after transaction commit.
     *
     * @param event The status change event
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleBacktestStatusChanged(BacktestStatusChangedEvent event) {
        log.info("Processing backtest status change: backtestId={}, userId={}, {} -> {}",
                event.backtestId(), event.userId(), event.oldStatus(), event.newStatus());

        try {
            switch (event.newStatus()) {
                case PREPARING -> handlePreparingTransition(event);
                case COMPLETED -> handleCompletedTransition(event);
                case FAILED -> handleFailedTransition(event);
                default -> log.debug("No handler for status transition to {}", event.newStatus());
            }
        } catch (Exception e) {
            log.error("Error handling backtest status change event: {}", event, e);
        }
    }

    /**
     * Handles transition to PREPARING status.
     * Starts the backtest execution workflow.
     */
    private void handlePreparingTransition(BacktestStatusChangedEvent event) {
        if (!event.oldStatus().canTransitionTo(BacktestStatus.PREPARING)) {
            log.warn("Unexpected transition to PREPARING from {}, expected CREATED", event.oldStatus());
            return;
        }

        log.info("Starting backtest execution workflow for backtest {} (user: {}, symbol: {})",
                event.backtestId(), event.userId(), event.symbol());

        // Verify user has credentials before starting
        try {
            credentialsService.getCredentials(event.userId());
        } catch (Exception e) {
            log.error("User {} does not have valid credentials, cannot start backtest {}",
                    event.userId(), event.backtestId(), e);
            return;
        }

        BacktestExecutionContext context = BacktestExecutionContext.builder()
                .backtestId(event.backtestId())
                .userId(event.userId())
                .baseStrategyId(event.baseStrategyId())
                .baseStrategyCode(event.baseStrategyCode())
                .symbol(event.symbol())
                .budget(event.budget())
                .testingStart(event.testingStart())
                .testingEnd(event.testingEnd())
                .build();

        // Start the Temporal workflow
        String workflowId = workflowBacktestExecutionService.startBacktestExecution(context);

        log.info("Started backtest execution workflow {} for backtest {} (user: {}, symbol: {})",
                workflowId, event.backtestId(), event.userId(), event.symbol());
    }

    /**
     * Handles transition to COMPLETED status.
     */
    private void handleCompletedTransition(BacktestStatusChangedEvent event) {
        log.info("Backtest {} completed successfully (user: {})",
                event.backtestId(), event.userId());
    }

    /**
     * Handles transition to FAILED status.
     */
    private void handleFailedTransition(BacktestStatusChangedEvent event) {
        log.warn("Backtest {} failed (user: {})",
                event.backtestId(), event.userId());
    }
}
