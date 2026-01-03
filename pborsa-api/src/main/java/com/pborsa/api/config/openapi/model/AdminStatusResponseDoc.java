package com.pborsa.api.config.openapi.model;

import com.pborsa.api.domain.dto.admin.AdminStatusResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(name = "AdminStatusApiResponse", description = "ApiResponse wrapper for admin status")
public class AdminStatusResponseDoc extends ApiSuccessResponseDocBase {

    @Schema(description = "Admin status payload")
    private AdminStatusResponse data;

    @Schema(description = "Optional message", example = "OK")
    private String message;
}

