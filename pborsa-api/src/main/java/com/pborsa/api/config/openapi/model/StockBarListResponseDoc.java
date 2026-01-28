package com.pborsa.api.config.openapi.model;

import com.pborsa.api.domain.dto.market.StockBarDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.List;

@Getter
@Schema(name = "StockBarListResponse", description = "ApiResponse wrapper for a list of stock bars")
public class StockBarListResponseDoc extends ApiSuccessResponseDocBase {

    @Schema(description = "List of OHLCV bars")
    private List<StockBarDto> data;

    @Schema(description = "Optional message", example = "OK")
    private String message;
}
