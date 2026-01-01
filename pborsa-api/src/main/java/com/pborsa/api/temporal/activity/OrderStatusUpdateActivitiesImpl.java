package com.pborsa.api.temporal.activity;

import com.pborsa.api.domain.dto.trading.OrderStatus;
import com.pborsa.api.domain.dto.trading.OrderStatusReason;
import com.pborsa.api.domain.dto.trading.OrderStatusUpdateRequest;
import com.pborsa.api.service.trading.OrderStatusMessageNormalizer;
import com.pborsa.api.service.trading.OrderPersistenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderStatusUpdateActivitiesImpl implements OrderStatusUpdateActivities {

    private final OrderPersistenceService orderPersistenceService;

    @Override
    public void updateOrderStatus(OrderStatusUpdateRequest request) {
        if (request == null || request.orderId() == null || request.status() == null) {
            log.warn("Skipping order status update, missing orderId or status");
            return;
        }
        String normalizedMessage = OrderStatusMessageNormalizer.normalize(request.message());
        OrderStatus status = request.status();
        OrderStatusReason reason = request.reason();
        UUID orderId = request.orderId();

        orderPersistenceService.updateStatus(orderId, status, normalizedMessage, reason);
        log.info("Updated order status from activity orderId={} status={} reason={} message={}",
                orderId, status, reason, normalizedMessage);
    }
}
