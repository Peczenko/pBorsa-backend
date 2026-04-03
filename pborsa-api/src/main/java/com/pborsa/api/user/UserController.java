package com.pborsa.api.user;

import com.pborsa.api.shared.config.openapi.ApiResponseDoc;
import com.pborsa.api.shared.config.openapi.model.ApiErrorResponseDoc;
import com.pborsa.api.shared.config.openapi.model.UserProfileResponseDoc;
import com.pborsa.api.shared.controller.ApiResponse;
import com.pborsa.api.shared.security.FirebaseUserPrincipal;
import com.pborsa.domain.dto.user.UserProfileDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User profile endpoints")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Returns the authenticated user profile")
    @ApiResponseDoc(code = "200", description = "Authenticated user profile", implementation = UserProfileResponseDoc.class)
    @ApiResponseDoc(code = "401", description = "Unauthorized", implementation = ApiErrorResponseDoc.class)
    public ResponseEntity<ApiResponse<UserProfileDto>> getCurrentUser(
            @AuthenticationPrincipal FirebaseUserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthorized"));
        }
        UserProfileDto profile = userService.syncUserProfile(principal);
        return ResponseEntity.ok(ApiResponse.success(profile));
    }
}
