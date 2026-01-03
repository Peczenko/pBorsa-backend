package com.pborsa.api.config.openapi.model;

import com.pborsa.api.domain.dto.strategy.StrategyDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.List;

@Getter
@Schema(name = "StrategyListResponse", description = "ApiResponse wrapper for a list of strategies")
public class StrategyListResponseDoc extends ApiSuccessResponseDocBase {

    @Schema(description = "List of strategies")
    private List<StrategyDto> data;

    @Schema(description = "Optional message", example = "OK")
    private String message;
}

