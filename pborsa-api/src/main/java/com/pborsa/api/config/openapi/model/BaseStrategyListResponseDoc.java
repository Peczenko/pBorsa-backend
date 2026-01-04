package com.pborsa.api.config.openapi.model;

import com.pborsa.api.domain.dto.strategy.BaseStrategyDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.List;

@Getter
@Schema(name = "BaseStrategyListResponse", description = "ApiResponse wrapper for a list of base strategies")
public class BaseStrategyListResponseDoc extends ApiSuccessResponseDocBase {

    @Schema(description = "List of base strategies")
    private List<BaseStrategyDto> data;

    @Schema(description = "Optional message", example = "OK")
    private String message;
}



