package com.pborsa.api.shared.config.openapi.model;

import com.pborsa.domain.dto.strategy.OrderDetailDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(name = "OrderDetailResponse", description = "ApiResponse wrapper for order details")
public class OrderDetailResponseDoc extends ApiSuccessResponseDocBase {

    @Schema(description = "Order detail payload")
    private OrderDetailDto data;

    @Schema(description = "Optional message", example = "OK")
    private String message;
}

