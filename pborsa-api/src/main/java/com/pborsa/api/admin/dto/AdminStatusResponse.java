package com.pborsa.api.admin.dto;

/**
 * Response DTO for admin status queries.
 */
public record AdminStatusResponse(
        Long userId,
        boolean admin
) {
}


