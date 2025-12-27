package com.pborsa.api.controller;

import com.pborsa.api.controller.response.ApiResponse;
import com.pborsa.api.domain.dto.strategy.StrategyExecutionStartRequest;
import com.pborsa.api.domain.dto.strategy.StrategyExecutionStartResponse;
import com.pborsa.api.service.strategy.StrategyExecutionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for strategy execution.
 */
@RestController
@RequestMapping("/api/v1/strategies")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Strategies", description = "Start strategy execution and streaming workflow")
public class StrategyExecutionController {

    private final StrategyExecutionService strategyExecutionService;

    @PostMapping("/{userId}/{strategyId}/start")
    @Operation(summary = "Start strategy execution", description = "Triggers a Temporal workflow to fetch historical bars and stream them to the trading engine")
    public ResponseEntity<ApiResponse<StrategyExecutionStartResponse>> startStrategyExecution(
            @PathVariable Long userId,
            @PathVariable String strategyId,
            @Valid @RequestBody StrategyExecutionStartRequest request
    ) {
        log.info("API startStrategyExecution userId={}, strategyId={}, symbol={}, timeframe={}",
                userId, strategyId, request.symbol(), request.timeframe());
        StrategyExecutionStartResponse response = strategyExecutionService.startStrategyExecution(
                userId,
                strategyId,
                request
        );
        return ResponseEntity.ok(ApiResponse.success(response, "Strategy execution started"));
    }
}
