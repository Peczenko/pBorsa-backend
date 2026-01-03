package com.pborsa.api.config.openapi.model;

import com.pborsa.api.domain.dto.strategy.StrategyDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(name = "StrategyResponse", description = "ApiResponse wrapper for a single strategy")
public class StrategyResponseDoc extends ApiSuccessResponseDocBase {

    @Schema(description = "Strategy payload")
    private StrategyDto data;

    @Schema(description = "Optional message", example = "OK")
    private String message;
}

