package com.pborsa.api.order.service;

import com.pborsa.domain.dto.trading.OrderStatus;
import com.pborsa.api.order.entity.OrderEntity;
import com.pborsa.api.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderReconciliationQueryService {

    private static final EnumSet<OrderStatus> TERMINAL_STATUSES = EnumSet.of(
            OrderStatus.FILLED,
            OrderStatus.CANCELED,
            OrderStatus.EXPIRED,
            OrderStatus.REJECTED
    );

    private final OrderRepository orderRepository;

    public List<OrderEntity> findCandidates(Instant end) {
        return orderRepository.findByUpdatedAtBeforeAndStatusNotIn(end, TERMINAL_STATUSES)
                .stream()
                .filter(order -> order.getUserId() != null)
                .toList();
    }
}
