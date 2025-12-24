package com.pborsa.api.controller;

import com.pborsa.api.controller.response.ApiResponse;
import com.pborsa.api.domain.dto.strategy.StrategyExecutionStartRequest;
import com.pborsa.api.domain.dto.strategy.StrategyExecutionStartResponse;
import com.pborsa.api.service.strategy.StrategyExecutionService;
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
public class StrategyExecutionController {

    private final StrategyExecutionService strategyExecutionService;

    @PostMapping("/{userId}/{strategyId}/start")
    public ResponseEntity<ApiResponse<StrategyExecutionStartResponse>> startStrategyExecution(
            @PathVariable String userId,
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
