package com.pborsa.api.domain.dto.admin;

/**
 * Response DTO for admin status queries.
 */
public record AdminStatusResponse(
        Long userId,
        boolean admin
) {
}


