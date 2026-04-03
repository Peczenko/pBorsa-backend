package com.pborsa.api.shared.config.openapi.model;

import com.pborsa.domain.dto.market.StockBarDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(name = "StockBarResponse", description = "ApiResponse wrapper for a single stock bar")
public class StockBarResponseDoc extends ApiSuccessResponseDocBase {

    @Schema(description = "OHLCV bar data")
    private StockBarDto data;

    @Schema(description = "Optional message", example = "OK")
    private String message;
}
