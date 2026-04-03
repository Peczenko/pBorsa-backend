package com.pborsa.api.backtest.service;

import com.pborsa.domain.dto.backtest.BacktestDto;
import com.pborsa.domain.dto.backtest.CreateBacktestRequest;
import com.pborsa.domain.dto.backtest.BacktestOrderDto;
import com.pborsa.domain.dto.backtest.BacktestSummaryDto;
import com.pborsa.api.backtest.entity.BacktestEntity;
import com.pborsa.api.backtest.entity.BacktestOrderEntity;
import com.pborsa.domain.entity.BacktestStatus;
import com.pborsa.api.strategy.entity.BaseStrategyEntity;
import com.pborsa.domain.event.BacktestStatusChangedEvent;
import com.pborsa.api.backtest.repository.BacktestOrderRepository;
import com.pborsa.api.backtest.repository.BacktestRepository;
import com.pborsa.api.backtest.mapper.BacktestMapper;
import com.pborsa.api.strategy.service.BaseStrategyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service for backtest CRUD operations.
 * Balance timeline calculation is delegated to {@link BacktestBalanceCalculator}.
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

    @Transactional(readOnly = true)
    public List<BacktestSummaryDto> getUserBacktests(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }

        log.debug("Getting all backtests for user: {}", userId);
        List<BacktestEntity> backtests = backtestRepository.findByUserIdOrderByCreatedAtDesc(userId);
        if (backtests.isEmpty()) {
            return List.of();
        }

        List<Long> backtestIds = backtests.stream()
                .map(BacktestEntity::getId)
                .toList();
        Map<Long, OrderCounts> countsByBacktestId = loadOrderCounts(backtestIds);

        return backtests.stream()
                .map(backtest -> {
                    OrderCounts counts = countsByBacktestId.getOrDefault(backtest.getId(), OrderCounts.empty());
                    return backtestMapper.toBacktestSummaryDto(backtest, counts.buyOrders(), counts.sellOrders());
                })
                .toList();
    }

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
                    Map<Long, OrderCounts> countsByBacktestId = loadOrderCounts(List.of(backtestId));
                    OrderCounts counts = countsByBacktestId.getOrDefault(backtestId, OrderCounts.empty());
                    return backtestMapper.toBacktestDto(backtest, counts.buyOrders(), counts.sellOrders());
                });
    }

    @Transactional(readOnly = true)
    public Optional<Page<BacktestOrderDto>> getBacktestOrders(Long userId, Long backtestId, Pageable pageable) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        if (backtestId == null) {
            throw new IllegalArgumentException("Backtest ID is required");
        }
        if (pageable == null) {
            throw new IllegalArgumentException("Pageable is required");
        }

        log.debug("Getting orders for backtest {} (user: {})", backtestId, userId);

        boolean exists = backtestRepository.findByIdAndUserId(backtestId, userId).isPresent();
        if (!exists) {
            return Optional.empty();
        }

        Page<BacktestOrderEntity> orders = backtestOrderRepository.findByBacktestId(backtestId, pageable);
        return Optional.of(orders.map(backtestMapper::toBacktestOrderDto));
    }

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

        BaseStrategyEntity baseStrategy = baseStrategyService.getStrategyEntityByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Base strategy not found: " + code));

        if (!baseStrategy.getActive()) {
            throw new IllegalArgumentException("Base strategy is not active: " + code);
        }

        if (request.testingStart().isAfter(request.testingEnd())) {
            throw new IllegalArgumentException("Testing start must be before testing end");
        }

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

        if (!oldStatus.canTransitionTo(BacktestStatus.PREPARING)) {
            throw new IllegalStateException(
                    "Backtest can only be started from CREATED status. Current status: " + oldStatus);
        }

        entity.setStatus(BacktestStatus.PREPARING);
        BacktestEntity saved = backtestRepository.save(entity);

        log.info("Backtest {} started for user {} (status: {} -> PREPARING)",
                backtestId, userId, oldStatus);

        publishStatusChangedEvent(saved, oldStatus, BacktestStatus.PREPARING);

        return Optional.of(backtestMapper.toBacktestDto(saved));
    }

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

        if (!canBeDeleted(entity.getStatus())) {
            throw new IllegalStateException(
                    "Backtest in status " + entity.getStatus() + " cannot be deleted.");
        }

        backtestRepository.delete(entity);
        log.info("Deleted backtest {} for user {}", backtestId, userId);

        return true;
    }

    // ==================== Private helpers ====================

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

    private Map<Long, OrderCounts> loadOrderCounts(List<Long> backtestIds) {
        if (backtestIds == null || backtestIds.isEmpty()) {
            return Map.of();
        }

        List<BacktestOrderRepository.BacktestOrderCounts> counts = backtestOrderRepository
                .findOrderCountsByBacktestIds(backtestIds);
        Map<Long, OrderCounts> result = new HashMap<>();

        for (BacktestOrderRepository.BacktestOrderCounts row : counts) {
            if (row == null || row.getBacktestId() == null) {
                continue;
            }
            int buyCount = toIntCount(row.getBuyCount());
            int sellCount = toIntCount(row.getSellCount());
            result.put(row.getBacktestId(), new OrderCounts(buyCount, sellCount));
        }

        return result;
    }

    private static int toIntCount(Long count) {
        if (count == null) {
            return 0;
        }
        return Math.toIntExact(count);
    }

    private record OrderCounts(int buyOrders, int sellOrders) {
        static OrderCounts empty() {
            return new OrderCounts(0, 0);
        }
    }
}
