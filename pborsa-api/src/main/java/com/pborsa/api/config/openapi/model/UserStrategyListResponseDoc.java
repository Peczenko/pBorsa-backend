package com.pborsa.api.config.openapi.model;

import com.pborsa.api.domain.dto.strategy.UserStrategyDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.List;

@Getter
@Schema(name = "UserStrategyListResponse", description = "ApiResponse wrapper for a list of user strategies")
public class UserStrategyListResponseDoc extends ApiSuccessResponseDocBase {

    @Schema(description = "List of user strategies")
    private List<UserStrategyDto> data;

    @Schema(description = "Optional message", example = "OK")
    private String message;
}



