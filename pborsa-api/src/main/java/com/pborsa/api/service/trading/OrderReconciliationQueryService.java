package com.pborsa.api.service.trading;

import com.pborsa.api.domain.dto.trading.OrderStatus;
import com.pborsa.api.domain.entity.OrderEntity;
import com.pborsa.api.repository.OrderRepository;
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
                .filter(order -> order.getUserId() != null && !order.getUserId().isBlank())
                .toList();
    }
}
