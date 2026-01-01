package com.pborsa.api.controller;

import com.pborsa.api.controller.response.ApiResponse;
import com.pborsa.api.domain.dto.strategy.StrategyExecutionStartRequest;
import com.pborsa.api.domain.dto.strategy.StrategyExecutionStartResponse;
import com.pborsa.api.service.strategy.StrategyService;
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


@RestController
@RequestMapping("/api/v1/strategies")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Strategies", description = "Controller for strategy operations")
public class StrategyExecutionController {

    private final StrategyService strategyService;

    @PostMapping("/{userId}/{strategyId}/start")
    @Operation(summary = "Start strategy execution", description = "Starts the execution of a trading strategy for a user. If execution starts temporal workflow can be found.")
    public ResponseEntity<ApiResponse<StrategyExecutionStartResponse>> startStrategyExecution(
            @PathVariable Long userId,
            @PathVariable Long strategyId,
            @Valid @RequestBody StrategyExecutionStartRequest request
    ) {
        log.info("API startStrategyExecution userId={}, strategyId={}, symbol={}",
                userId, strategyId, request.symbol());
        StrategyExecutionStartResponse response = strategyService.startStrategy(
                userId,
                strategyId,
                request
        );
        return ResponseEntity.ok(ApiResponse.success(response, "Strategy execution started"));
    }
}
