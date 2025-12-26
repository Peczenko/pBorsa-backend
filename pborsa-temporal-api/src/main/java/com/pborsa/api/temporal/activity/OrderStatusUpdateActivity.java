package com.pborsa.api.temporal.activity;

import com.pborsa.api.domain.dto.trading.OrderStatus;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

import java.util.UUID;

@ActivityInterface
public interface OrderStatusUpdateActivity {

    @ActivityMethod
    void updateOrderStatus(UUID orderId, OrderStatus status, String message);
}
