package com.pborsa.api.temporal.workflow;

import com.pborsa.api.domain.dto.trading.OrderRequest;
import com.pborsa.api.domain.dto.trading.OrderResponse;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

import java.util.List;

@WorkflowInterface
public interface BatchTradeExecutionWorkflow {

    @WorkflowMethod(name = "executeBatchTrades")
    List<OrderResponse> executeBatchTrades(String userId, List<OrderRequest> orders);
}
