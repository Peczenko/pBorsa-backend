package com.pborsa.api.controller;

import com.pborsa.api.config.openapi.ApiResponseDoc;
import com.pborsa.api.config.openapi.model.ApiErrorResponseDoc;
import com.pborsa.api.config.openapi.model.BaseStrategyListResponseDoc;
import com.pborsa.api.config.openapi.model.BaseStrategyResponseDoc;
import com.pborsa.api.controller.response.ApiResponse;
import com.pborsa.api.domain.dto.strategy.BaseStrategyDto;
import com.pborsa.api.service.strategy.BaseStrategyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for base strategy catalog (read-only).
 * These are the strategy templates that users can subscribe to.
 */
@RestController
@RequestMapping("/api/v1/strategies")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Base Strategies", description = "Strategy catalog endpoints (read-only)")
public class BaseStrategyController {

    private final BaseStrategyService baseStrategyService;

    /**
     * Gets all available base strategies.
     *
     * @return List of active base strategies
     */
    @GetMapping
    @Operation(summary = "List base strategies", description = "Gets all available base strategy templates from the catalog.")
    @ApiResponseDoc(code = "200", description = "Base strategies retrieved successfully", implementation = BaseStrategyListResponseDoc.class)
    public ResponseEntity<ApiResponse<List<BaseStrategyDto>>> getAllStrategies() {
        List<BaseStrategyDto> strategies = baseStrategyService.getAllActiveStrategies();
        return ResponseEntity.ok(ApiResponse.success(strategies));
    }

    /**
     * Gets a base strategy by code.
     *
     * @param code Strategy code (e.g., MOMENTUM_V1)
     * @return Base strategy details
     */
    @GetMapping("/{code}")
    @Operation(summary = "Get base strategy by code", description = "Gets a specific base strategy template by its code.")
    @ApiResponseDoc(code = "200", description = "Base strategy retrieved successfully", implementation = BaseStrategyResponseDoc.class)
    @ApiResponseDoc(code = "404", description = "Strategy not found", implementation = ApiErrorResponseDoc.class)
    public ResponseEntity<ApiResponse<BaseStrategyDto>> getStrategyByCode(
            @Parameter(description = "Strategy code (e.g., MOMENTUM_V1)") @PathVariable String code) {
        return baseStrategyService.getStrategyByCode(code)
                .map(strategy -> ResponseEntity.ok(ApiResponse.success(strategy)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Strategy not found: " + code)));
    }
}

