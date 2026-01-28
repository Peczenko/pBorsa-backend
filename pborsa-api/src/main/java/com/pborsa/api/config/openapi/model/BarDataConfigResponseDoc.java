package com.pborsa.api.config.openapi.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.List;

@Getter
@Schema(name = "BarDataConfigResponse", description = "ApiResponse wrapper for bar data configuration")
public class BarDataConfigResponseDoc extends ApiSuccessResponseDocBase {

    @Schema(description = "Bar data configuration")
    private BarDataConfigPayload data;

    @Schema(description = "Optional message", example = "OK")
    private String message;

    @Getter
    @Schema(name = "BarDataConfig", description = "Bar data configuration settings")
    public static class BarDataConfigPayload {

        @Schema(description = "List of supported timeframes", example = "[\"1Min\", \"5Min\", \"15Min\", \"30Min\", \"1Hour\", \"4Hour\", \"1Day\", \"1Week\"]")
        private List<String> supportedTimeframes;

        @Schema(description = "Default timeframe", example = "5Min")
        private String defaultTimeframe;

        @Schema(description = "Default number of bars to return", example = "500")
        private int defaultBarsLimit;

        @Schema(description = "Maximum bars allowed per request", example = "5000")
        private int maxBarsPerRequest;

        @Schema(description = "Update interval for live streaming in seconds", example = "30")
        private long updateIntervalSeconds;
    }
}
