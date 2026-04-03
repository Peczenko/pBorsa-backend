package com.pborsa.api.order.service;

import com.pborsa.api.order.entity.OrderHistoryEntity;
import com.pborsa.api.order.mapper.OrderHistoryMapper;
import com.pborsa.domain.dto.strategy.OrderHistoryDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for querying order history.
 * Handles business logic and mapping for order history queries.
 * Security: Validates userId, ensures history belongs to user's orders.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderHistoryQueryService {

    private final OrderHistoryPersistenceService orderHistoryPersistenceService;
    private final OrderQueryService orderQueryService;
    private final OrderHistoryMapper orderHistoryMapper;

    /**
     * Gets order history for an order.
     * Security: Validates userId, ensures history belongs to user's orders.
     *
     * @param userId  User ID
     * @param orderId Order ID
     * @return List of order history DTOs
     */
    public List<OrderHistoryDto> getOrderHistory(Long userId, UUID orderId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        if (orderId == null) {
            throw new IllegalArgumentException("Order ID is required");
        }

        log.debug("Getting order history for order {} and user {}", orderId, userId);

        // Security check: verify order belongs to user
        Optional<com.pborsa.api.order.entity.OrderEntity> order = orderQueryService.findById(orderId);
        if (order.isEmpty()) {
            log.warn("Order {} not found", orderId);
            return List.of();
        }

        com.pborsa.api.order.entity.OrderEntity orderEntity = order.get();
        if (!orderEntity.getUserId().equals(userId)) {
            log.warn("User {} attempted to access order {} history belonging to user {}", userId, orderId, orderEntity.getUserId());
            return List.of();
        }

        List<OrderHistoryEntity> history = orderHistoryPersistenceService.findHistoryByUserIdAndOrderId(userId, orderId);
        return orderHistoryMapper.toOrderHistoryDtoList(history);
    }

    /**
     * Gets a specific order history entry.
     * Security: Validates userId, ensures history belongs to user's orders.
     *
     * @param userId    User ID
     * @param historyId History entry ID
     * @return Optional order history DTO
     */
    public Optional<OrderHistoryDto> getOrderHistoryEntry(Long userId, UUID historyId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        if (historyId == null) {
            throw new IllegalArgumentException("History ID is required");
        }

        log.debug("Getting order history entry {} for user {}", historyId, userId);

        Optional<OrderHistoryEntity> history = orderHistoryPersistenceService.findHistoryById(historyId);
        if (history.isEmpty()) {
            return Optional.empty();
        }

        OrderHistoryEntity historyEntity = history.get();
        // Security check: verify history belongs to user
        if (!historyEntity.getUserId().equals(userId)) {
            log.warn("User {} attempted to access history {} belonging to user {}", userId, historyId, historyEntity.getUserId());
            return Optional.empty();
        }

        return Optional.of(orderHistoryMapper.toOrderHistoryDto(historyEntity));
    }

    /**
     * Gets order history for an order without user validation.
     * Admin only - no security check performed here.
     *
     * @param orderId Order ID
     * @return List of order history DTOs
     */
    public List<OrderHistoryDto> getOrderHistoryAdmin(UUID orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("Order ID is required");
        }

        log.debug("Admin getting order history for order {}", orderId);
        List<OrderHistoryEntity> history = orderHistoryPersistenceService.findHistoryByOrderId(orderId);
        return orderHistoryMapper.toOrderHistoryDtoList(history);
    }

    /**
     * Gets a specific order history entry without user validation.
     * Admin only - no security check performed here.
     *
     * @param historyId History entry ID
     * @return Optional order history DTO
     */
    public Optional<OrderHistoryDto> getOrderHistoryEntryAdmin(UUID historyId) {
        if (historyId == null) {
            throw new IllegalArgumentException("History ID is required");
        }

        log.debug("Admin getting order history entry {}", historyId);
        return orderHistoryPersistenceService.findHistoryById(historyId)
                .map(orderHistoryMapper::toOrderHistoryDto);
    }
}


