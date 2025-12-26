package com.pborsa.api.temporal.activity;

import com.pborsa.api.domain.dto.trading.OrderStatus;
import com.pborsa.api.service.trading.OrderPersistenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderStatusUpdateActivityImpl implements OrderStatusUpdateActivity {

    private final OrderPersistenceService orderPersistenceService;

    @Override
    public void updateOrderStatus(UUID orderId, OrderStatus status, String message) {
        orderPersistenceService.updateStatus(orderId, status, message);
        log.info("Updated order status orderId={} status={} message={}", orderId, status, message);
    }
}
