package com.pborsa.api.config.openapi.model;

import com.pborsa.api.domain.dto.strategy.UserStrategyDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(name = "UserStrategyResponse", description = "ApiResponse wrapper for a single user strategy")
public class UserStrategyResponseDoc extends ApiSuccessResponseDocBase {

    @Schema(description = "User strategy payload")
    private UserStrategyDto data;

    @Schema(description = "Optional message", example = "OK")
    private String message;
}



