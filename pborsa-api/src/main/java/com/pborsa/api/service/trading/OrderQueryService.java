package com.pborsa.api.service.trading;

import com.pborsa.api.domain.dto.trading.OrderStatus;
import com.pborsa.api.domain.entity.OrderEntity;
import com.pborsa.api.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service for querying orders.
 * Separated from persistence logic to follow single responsibility principle.
 */
@Service
@RequiredArgsConstructor
public class OrderQueryService {

    private static final EnumSet<OrderStatus> TERMINAL_STATUSES = EnumSet.of(
            OrderStatus.FILLED,
            OrderStatus.CANCELED,
            OrderStatus.EXPIRED,
            OrderStatus.REJECTED
    );

    private final OrderRepository orderRepository;

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
}

