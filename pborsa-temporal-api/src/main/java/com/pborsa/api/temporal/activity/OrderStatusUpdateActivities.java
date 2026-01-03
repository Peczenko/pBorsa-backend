package com.pborsa.api.temporal.activity;

import com.pborsa.api.domain.dto.trading.OrderStatusUpdateRequest;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

/**
 * Temporal activities interface for updating order status in the API app.
 */
@ActivityInterface
public interface OrderStatusUpdateActivities {

    @ActivityMethod
    void updateOrderStatus(OrderStatusUpdateRequest request);
}
