package com.pborsa.api.shared.config.openapi.model;

import com.pborsa.domain.dto.strategy.OrderHistoryDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(name = "OrderHistoryResponse", description = "ApiResponse wrapper for a single order history entry")
public class OrderHistoryResponseDoc extends ApiSuccessResponseDocBase {

    @Schema(description = "Order history entry payload")
    private OrderHistoryDto data;

    @Schema(description = "Optional message", example = "OK")
    private String message;
}

