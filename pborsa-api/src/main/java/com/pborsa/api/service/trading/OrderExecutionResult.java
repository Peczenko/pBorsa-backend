package com.pborsa.api.service.trading;

public record OrderExecutionResult(boolean accepted, String workflowId, String orderId, String message) {
    public static OrderExecutionResult accepted(String workflowId, String orderId) {
        return new OrderExecutionResult(true, workflowId, orderId, null);
    }

    public static OrderExecutionResult rejected(String orderId, String message) {
        return new OrderExecutionResult(false, null, orderId, message);
    }
}
