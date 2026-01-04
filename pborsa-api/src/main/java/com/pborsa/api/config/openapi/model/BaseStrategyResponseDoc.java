package com.pborsa.api.config.openapi.model;

import com.pborsa.api.domain.dto.strategy.BaseStrategyDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(name = "BaseStrategyResponse", description = "ApiResponse wrapper for a single base strategy")
public class BaseStrategyResponseDoc extends ApiSuccessResponseDocBase {

    @Schema(description = "Base strategy payload")
    private BaseStrategyDto data;

    @Schema(description = "Optional message", example = "OK")
    private String message;
}



