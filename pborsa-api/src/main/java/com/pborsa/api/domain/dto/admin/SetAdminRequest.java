package com.pborsa.api.domain.dto.admin;

import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for setting admin status.
 */
public record SetAdminRequest(
        @NotNull(message = "admin flag is required")
        Boolean admin
) {
}


