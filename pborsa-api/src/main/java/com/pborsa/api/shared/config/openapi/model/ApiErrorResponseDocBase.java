package com.pborsa.api.shared.config.openapi.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.Instant;

@Getter
@Schema(name = "ApiErrorResponseBase", description = "Common ApiResponse fields for error responses")
public abstract class ApiErrorResponseDocBase {

    @Schema(description = "Indicates whether the request succeeded", example = "false")
    private boolean success;

    @Schema(description = "Error code", example = "UNAUTHORIZED")
    private String error;

    @Schema(description = "Human-readable message", example = "Unauthorized")
    private String message;

    @Schema(description = "Response timestamp in UTC")
    private Instant timestamp;
}
