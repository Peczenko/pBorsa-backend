package com.pborsa.api.config.openapi.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.Instant;

@Getter
@Schema(name = "ApiSuccessResponseBase", description = "Common ApiResponse fields for successful responses")
public abstract class ApiSuccessResponseDocBase {

    @Schema(description = "Indicates whether the request succeeded", example = "true")
    private boolean success;

    @Schema(description = "Response timestamp in UTC")
    private Instant timestamp;
}
