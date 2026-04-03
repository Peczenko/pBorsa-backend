package com.pborsa.api.backtest.temporal.activity;

import com.pborsa.temporal.activity.BacktestActivities;
import com.pborsa.api.tradingengine.BacktestClient;
import com.pborsa.domain.dto.backtest.BacktestExecutionContext;
import com.pborsa.domain.dto.backtest.BacktestOrderDto;
import com.pborsa.domain.dto.backtest.BacktestResultDto;
import com.pborsa.domain.dto.market.StockBarDto;
import com.pborsa.api.backtest.entity.BacktestEntity;
import com.pborsa.api.backtest.entity.BacktestOrderEntity;
import com.pborsa.domain.entity.BacktestStatus;
import com.pborsa.api.backtest.repository.BacktestRepository;
import com.pborsa.api.backtest.service.BacktestDataFetcher;
import io.temporal.activity.Activity;
import io.temporal.activity.ActivityExecutionContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.jacobpeterson.alpaca.openapi.marketdata.ApiException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Activity implementation for backtest execution.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BacktestActivitiesImpl implements BacktestActivities {

    private final BacktestDataFetcher dataFetcher;
    private final BacktestClient backtestClient;
    private final BacktestRepository backtestRepository;

    @Override
    public BacktestResultDto executeBacktest(BacktestExecutionContext context) {
        log.info("Activity executing backtest {} for strategy {}",
                context.backtestId(), context.baseStrategyCode());

        ActivityExecutionContext activityContext = Activity.getExecutionContext();
        Runnable heartbeat = () -> activityContext.heartbeat(context.backtestId());

        try {
            // Update status to RUNNING
            updateStatus(context.backtestId(), BacktestStatus.RUNNING);

            // Fetch historical data before testing period
            List<StockBarDto> historyBeforeStart = dataFetcher.fetchHistoryBeforeStart(
                    context.userId(),
                    context.symbol(),
                    context.testingStart(),
                    heartbeat
            );
            log.info("Backtest {}: fetched {} bars before testing start",
                    context.backtestId(), historyBeforeStart.size());

            heartbeat.run();

            // Fetch historical data for testing period
            List<StockBarDto> historyTestingRange = dataFetcher.fetchHistoryTestingRange(
                    context.userId(),
                    context.symbol(),
                    context.testingStart(),
                    context.testingEnd(),
                    heartbeat
            );
            log.info("Backtest {}: fetched {} bars for testing range",
                    context.backtestId(), historyTestingRange.size());

            heartbeat.run();

            // Execute backtest on trading engine
            BacktestResultDto result = backtestClient.executeBacktest(
                    context,
                    historyBeforeStart,
                    historyTestingRange
            );

            log.info("Backtest {} completed: success={}, trades={}, pnl={}",
                    context.backtestId(), result.success(), result.totalTrades(), result.pnl());

            return result;

        } catch (ApiException e) {
            log.error("Failed to fetch historical data for backtest {}", context.backtestId(), e);
            throw new RuntimeException("Failed to fetch historical data: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void storeResults(BacktestResultDto result) {
        log.info("Activity storing results for backtest {}", result.backtestId());

        BacktestEntity backtest = backtestRepository.findById(result.backtestId())
                .orElseThrow(() -> new IllegalArgumentException("Backtest not found: " + result.backtestId()));

        // Update metrics
        backtest.setPnl(result.pnl());
        backtest.setMaxDrawdown(result.maxDrawdown());
        backtest.setTotalTrades(result.totalTrades());
        backtest.setWinningTrades(result.winningTrades());

        if (!result.success()) {
            backtest.setErrorMessage(result.message());
        }

        // Store orders
        if (result.orders() != null) {
            for (BacktestOrderDto orderDto : result.orders()) {
                BacktestOrderEntity order = new BacktestOrderEntity()
                        .setSymbol(orderDto.symbol())
                        .setSide(orderDto.side())
                        .setQuantity(orderDto.quantity())
                        .setPrice(orderDto.price())
                        .setExecutedAt(orderDto.executedAt());
                backtest.addOrder(order);
            }
        }

        backtestRepository.save(backtest);
        log.info("Backtest {} results stored: {} orders", result.backtestId(),
                result.orders() != null ? result.orders().size() : 0);
    }

    @Override
    @Transactional
    public void markCompleted(Long backtestId) {
        log.info("Activity marking backtest {} as completed", backtestId);

        BacktestEntity backtest = backtestRepository.findById(backtestId)
                .orElseThrow(() -> new IllegalArgumentException("Backtest not found: " + backtestId));

        backtest.setStatus(BacktestStatus.COMPLETED);
        backtest.setCompletedAt(Instant.now());
        backtestRepository.save(backtest);

        log.info("Backtest {} marked as completed", backtestId);
    }

    @Override
    @Transactional
    public void markFailed(Long backtestId, String errorMessage) {
        log.info("Activity marking backtest {} as failed: {}", backtestId, errorMessage);

        BacktestEntity backtest = backtestRepository.findById(backtestId)
                .orElseThrow(() -> new IllegalArgumentException("Backtest not found: " + backtestId));

        backtest.setStatus(BacktestStatus.FAILED);
        backtest.setErrorMessage(truncateMessage(errorMessage, 1024));
        backtest.setCompletedAt(Instant.now());
        backtestRepository.save(backtest);

        log.info("Backtest {} marked as failed", backtestId);
    }

    @Transactional
    void updateStatus(Long backtestId, BacktestStatus status) {
        BacktestEntity backtest = backtestRepository.findById(backtestId)
                .orElseThrow(() -> new IllegalArgumentException("Backtest not found: " + backtestId));
        backtest.setStatus(status);
        backtestRepository.save(backtest);
    }

    private String truncateMessage(String message, int maxLength) {
        if (message == null) {
            return null;
        }
        return message.length() > maxLength ? message.substring(0, maxLength) : message;
    }
}
