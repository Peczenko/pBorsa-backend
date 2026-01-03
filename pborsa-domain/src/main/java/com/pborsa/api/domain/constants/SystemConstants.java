package com.pborsa.api.domain.constants;

/**
 * System-wide constants.
 */
public final class SystemConstants {

    /**
     * Special user ID used to identify system credentials in the database.
     * System credentials are stored in user_api_credentials table with userId = SYSTEM_USER_ID.
     */
    public static final Long SYSTEM_USER_ID = 0L;

    private SystemConstants() {
        // Utility class - prevent instantiation
    }
}


