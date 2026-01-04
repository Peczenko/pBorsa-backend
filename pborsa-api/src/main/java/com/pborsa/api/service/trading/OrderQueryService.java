package com.pborsa.api.service.trading;

import com.pborsa.api.domain.dto.strategy.OrderDetailDto;
import com.pborsa.api.domain.dto.trading.OrderStatus;
import com.pborsa.api.domain.entity.OrderEntity;
import com.pborsa.api.repository.OrderRepository;
import com.pborsa.api.service.mapper.OrderDetailMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service for querying orders.
 * Separated from persistence logic to follow single responsibility principle.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderQueryService {

    private static final EnumSet<OrderStatus> TERMINAL_STATUSES = EnumSet.of(
            OrderStatus.FILLED,
            OrderStatus.CANCELED,
            OrderStatus.EXPIRED,
            OrderStatus.REJECTED
    );

    private final OrderRepository orderRepository;
    private final OrderDetailMapper orderDetailMapper;

    public Optional<OrderEntity> findById(UUID id) {
        return orderRepository.findById(id);
    }

    public Optional<OrderEntity> findByExternalIds(String alpacaOrderId, String clientOrderId) {
        if (alpacaOrderId != null && !alpacaOrderId.isBlank()) {
            Optional<OrderEntity> byAlpaca = orderRepository.findByAlpacaOrderId(alpacaOrderId);
            if (byAlpaca.isPresent()) {
                return byAlpaca;
            }
        }
        if (clientOrderId != null && !clientOrderId.isBlank()) {
            return orderRepository.findByClientOrderId(clientOrderId);
        }
        return Optional.empty();
    }

    public boolean hasOpenOrders(Long userId) {
        return orderRepository.countByUserIdAndStatusNotIn(userId, TERMINAL_STATUSES) > 0;
    }

    public List<Long> findUsersWithOpenOrders() {
        return orderRepository.findDistinctUserIdByStatusNotIn(TERMINAL_STATUSES)
                .stream()
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * Gets orders for a specific user strategy.
     * Security: Always validates userId, ensures orders belong to the user.
     *
     * @param userId         User ID
     * @param userStrategyId User strategy ID
     * @return List of order detail DTOs
     */
    public List<OrderDetailDto> getOrdersByUserStrategyId(Long userId, Long userStrategyId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        if (userStrategyId == null) {
            throw new IllegalArgumentException("User strategy ID is required");
        }

        log.debug("Getting orders for user {} and user strategy {}", userId, userStrategyId);
        List<OrderEntity> orders = orderRepository.findByUserIdAndUserStrategyId(userId, userStrategyId);
        return orderDetailMapper.toOrderDetailDtoList(orders);
    }

    /**
     * Gets order details by order ID.
     * Security: Always validates userId, ensures order belongs to the user.
     *
     * @param userId  User ID
     * @param orderId Order ID
     * @return Optional order detail DTO
     */
    public Optional<OrderDetailDto> getOrderDetail(Long userId, UUID orderId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        if (orderId == null) {
            throw new IllegalArgumentException("Order ID is required");
        }

        log.debug("Getting order {} for user {}", orderId, userId);
        Optional<OrderEntity> order = orderRepository.findById(orderId);

        if (order.isEmpty()) {
            return Optional.empty();
        }

        OrderEntity orderEntity = order.get();
        // Security check: ensure order belongs to the user
        if (!orderEntity.getUserId().equals(userId)) {
            log.warn("User {} attempted to access order {} belonging to user {}", userId, orderId, orderEntity.getUserId());
            return Optional.empty();
        }

        return Optional.of(orderDetailMapper.toOrderDetailDto(orderEntity));
    }

    /**
     * Gets order details by order ID without user validation.
     * Admin only - no security check performed here.
     *
     * @param orderId Order ID
     * @return Optional order detail DTO
     */
    public Optional<OrderDetailDto> getOrderDetailAdmin(UUID orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("Order ID is required");
        }

        log.debug("Admin getting order {}", orderId);
        return orderRepository.findById(orderId)
                .map(orderDetailMapper::toOrderDetailDto);
    }
}

