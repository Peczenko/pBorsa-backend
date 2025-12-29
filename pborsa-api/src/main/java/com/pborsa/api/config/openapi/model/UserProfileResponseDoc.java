package com.pborsa.api.config.openapi.model;

import com.pborsa.api.domain.dto.user.UserProfileDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(name = "UserProfileResponse", description = "ApiResponse wrapper for the current user profile")
public class UserProfileResponseDoc extends ApiSuccessResponseDocBase {

    @Schema(description = "User profile payload")
    private UserProfileDto data;

    @Schema(description = "Optional message", example = "OK")
    private String message;

}
