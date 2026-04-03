package com.pborsa.api.backtest.service;

import com.pborsa.domain.dto.backtest.BacktestBalancePointDto;
import com.pborsa.api.backtest.entity.BacktestEntity;
import com.pborsa.api.backtest.entity.BacktestOrderEntity;
import com.pborsa.api.backtest.repository.BacktestOrderRepository;
import com.pborsa.api.backtest.repository.BacktestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Calculates balance timeline for backtests based on executed orders.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BacktestBalanceCalculator {

    private final BacktestRepository backtestRepository;
    private final BacktestOrderRepository backtestOrderRepository;

    /**
     * Gets balance timeline for a backtest based on executed orders.
     *
     * @param userId     User ID
     * @param backtestId Backtest ID
     * @return Optional list of balance points
     */
    @Transactional(readOnly = true)
    public Optional<List<BacktestBalancePointDto>> getBacktestBalanceTimeline(Long userId, Long backtestId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        if (backtestId == null) {
            throw new IllegalArgumentException("Backtest ID is required");
        }

        log.debug("Getting balance timeline for backtest {} (user: {})", backtestId, userId);

        Optional<BacktestEntity> optBacktest = backtestRepository.findByIdAndUserId(backtestId, userId);
        if (optBacktest.isEmpty()) {
            return Optional.empty();
        }

        BacktestEntity backtest = optBacktest.get();
        List<BacktestOrderEntity> orders = backtestOrderRepository
                .findByBacktestIdOrderByExecutedAtAsc(backtestId);

        BigDecimal cash = backtest.getBudget() != null ? backtest.getBudget() : BigDecimal.ZERO;
        BigDecimal shares = BigDecimal.ZERO;
        List<BacktestBalancePointDto> points = new ArrayList<>(orders.size());

        for (BacktestOrderEntity order : orders) {
            if (order.getQuantity() == null || order.getPrice() == null) {
                throw new IllegalStateException("Backtest order missing quantity or price: " + order.getId());
            }
            if (order.getExecutedAt() == null) {
                throw new IllegalStateException("Backtest order missing executedAt: " + order.getId());
            }

            String side = normalizeOrderSide(order.getSide());
            BigDecimal notional = order.getPrice().multiply(order.getQuantity());

            if ("BUY".equals(side)) {
                cash = cash.subtract(notional);
                shares = shares.add(order.getQuantity());
            } else if ("SELL".equals(side)) {
                cash = cash.add(notional);
                shares = shares.subtract(order.getQuantity());
            }

            BigDecimal balance = cash.add(shares.multiply(order.getPrice()));
            points.add(new BacktestBalancePointDto(order.getExecutedAt(), balance));
        }

        return Optional.of(points);
    }

    private static String normalizeOrderSide(String side) {
        if (side == null || side.isBlank()) {
            throw new IllegalStateException("Backtest order side is required");
        }
        String normalized = side.trim().toUpperCase(Locale.ROOT);
        if (!"BUY".equals(normalized) && !"SELL".equals(normalized)) {
            throw new IllegalStateException("Unsupported backtest order side: " + side);
        }
        return normalized;
    }
}
