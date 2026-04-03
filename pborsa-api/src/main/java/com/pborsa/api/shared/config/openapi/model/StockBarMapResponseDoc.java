package com.pborsa.api.shared.config.openapi.model;

import com.pborsa.domain.dto.market.StockBarDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.Map;

@Getter
@Schema(name = "StockBarMapResponse", description = "ApiResponse wrapper for a map of symbol to stock bar")
public class StockBarMapResponseDoc extends ApiSuccessResponseDocBase {

    @Schema(description = "Map of symbol to latest OHLCV bar")
    private Map<String, StockBarDto> data;

    @Schema(description = "Optional message", example = "OK")
    private String message;
}
