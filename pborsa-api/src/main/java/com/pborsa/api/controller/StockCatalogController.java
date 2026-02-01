package com.pborsa.api.controller;

import com.pborsa.api.controller.response.ApiResponse;
import com.pborsa.api.domain.constants.AvailableStocks;
import com.pborsa.api.domain.dto.market.StockInfoDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller for available stock symbols in the application.
 */
@RestController
@RequestMapping("/api/v1/stocks")
@Tag(name = "Stocks", description = "Supported stock symbols for the application")
public class StockCatalogController {

    /**
     * Gets the static list of supported stocks.
     */
    @GetMapping
    @Operation(summary = "List available stocks", description = "Returns the static list of supported stocks.")
    public ResponseEntity<ApiResponse<List<StockInfoDto>>> getAvailableStocks() {
        return ResponseEntity.ok(ApiResponse.success(AvailableStocks.AVAILABLE_STOCKS));
    }
}
