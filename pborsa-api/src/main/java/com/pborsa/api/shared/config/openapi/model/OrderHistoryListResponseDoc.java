package com.pborsa.api.shared.config.openapi.model;

import com.pborsa.domain.dto.strategy.OrderHistoryDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.List;

@Getter
@Schema(name = "OrderHistoryListResponse", description = "ApiResponse wrapper for a list of order history entries")
public class OrderHistoryListResponseDoc extends ApiSuccessResponseDocBase {

    @Schema(description = "List of order history entries")
    private List<OrderHistoryDto> data;

    @Schema(description = "Optional message", example = "OK")
    private String message;
}

