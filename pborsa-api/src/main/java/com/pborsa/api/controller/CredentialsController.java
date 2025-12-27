package com.pborsa.api.controller;

import com.pborsa.api.controller.response.ApiResponse;
import com.pborsa.api.domain.dto.credentials.CredentialsRegistrationRequest;
import com.pborsa.api.service.credentials.UserCredentialsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for managing user API credentials.
 */
@RestController
@RequestMapping("/api/v1/credentials")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Credentials", description = "Manage Alpaca API credentials for users")
public class CredentialsController {

    private final UserCredentialsService credentialsService;

    /**
     * Registers or updates user's Alpaca API credentials.
     */
    @PostMapping("/{userId}")
    @Operation(summary = "Register credentials", description = "Creates or updates Alpaca API keys for the user")
    public ResponseEntity<ApiResponse<Boolean>> registerCredentials(
            @PathVariable Long userId,
            @Valid @RequestBody CredentialsRegistrationRequest request
    ) {
        log.info("Registering credentials for user: {}", userId);
        boolean success = credentialsService.registerCredentials(userId, request);
        
        if (success) {
            return ResponseEntity.ok(ApiResponse.success(true, "Credentials registered successfully"));
        } else {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid credentials - verification failed"));
        }
    }

    /**
     * Checks if user has registered credentials.
     */
    @GetMapping("/{userId}/status")
    @Operation(summary = "Check credentials status", description = "Returns true if credentials are present and active")
    public ResponseEntity<ApiResponse<Boolean>> hasCredentials(@PathVariable Long userId) {
        boolean hasCredentials = credentialsService.hasCredentials(userId);
        return ResponseEntity.ok(ApiResponse.success(hasCredentials));
    }

    /**
     * Deactivates user's credentials.
     */
    @DeleteMapping("/{userId}")
    @Operation(summary = "Deactivate credentials", description = "Soft-deactivates stored keys for the user")
    public ResponseEntity<ApiResponse<Void>> deactivateCredentials(@PathVariable Long userId) {
        log.info("Deactivating credentials for user: {}", userId);
        credentialsService.deactivateCredentials(userId);
        return ResponseEntity.ok(ApiResponse.success("Credentials deactivated successfully"));
    }

    /**
     * Refreshes the cached credentials.
     */
    @PostMapping("/{userId}/refresh")
    @Operation(summary = "Refresh credentials cache", description = "Reloads cached credentials from storage")
    public ResponseEntity<ApiResponse<Void>> refreshCredentials(@PathVariable Long userId) {
        credentialsService.refreshCredentials(userId);
        return ResponseEntity.ok(ApiResponse.success("Credentials cache refreshed"));
    }
}

