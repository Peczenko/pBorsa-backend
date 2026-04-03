package com.pborsa.api.order.service;

import com.pborsa.api.order.entity.OrderHistoryEntity;
import com.pborsa.api.order.repository.OrderHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistence service for order history data access.
 * Handles all database operations for order history.
 * Single responsibility: Data access only, no business logic.
 * Security: Always requires userId parameter for user-scoped queries.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderHistoryPersistenceService {

    private final OrderHistoryRepository orderHistoryRepository;

    /**
     * Finds order history by order ID.
     * Note: This method does not filter by userId - use with caution.
     * Prefer findByUserIdAndOrderId for user-scoped queries.
     *
     * @param orderId Order ID
     * @return List of order history entries
     */
    @Transactional(readOnly = true)
    public List<OrderHistoryEntity> findHistoryByOrderId(UUID orderId) {
        log.debug("Finding order history by order ID: {}", orderId);
        return orderHistoryRepository.findByOrderIdOrderByCreatedAtAsc(orderId);
    }

    /**
     * Finds order history by user ID and order ID.
     * This is the preferred method for user-scoped queries.
     *
     * @param userId  User ID
     * @param orderId Order ID
     * @return List of order history entries
     */
    @Transactional(readOnly = true)
    public List<OrderHistoryEntity> findHistoryByUserIdAndOrderId(Long userId, UUID orderId) {
        log.debug("Finding order history by user ID: {} and order ID: {}", userId, orderId);
        return orderHistoryRepository.findByUserIdAndOrderId(userId, orderId);
    }

    /**
     * Finds a specific order history entry by ID.
     *
     * @param historyId History entry ID
     * @return Optional order history entity
     */
    @Transactional(readOnly = true)
    public Optional<OrderHistoryEntity> findHistoryById(UUID historyId) {
        log.debug("Finding order history by ID: {}", historyId);
        return orderHistoryRepository.findById(historyId);
    }
}


