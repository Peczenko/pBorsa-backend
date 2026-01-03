package com.pborsa.api.config.openapi.model;

import com.pborsa.api.domain.dto.strategy.OrderDetailDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.List;

@Getter
@Schema(name = "OrderDetailListResponse", description = "ApiResponse wrapper for a list of order details")
public class OrderDetailListResponseDoc extends ApiSuccessResponseDocBase {

    @Schema(description = "List of order details")
    private List<OrderDetailDto> data;

    @Schema(description = "Optional message", example = "OK")
    private String message;
}

