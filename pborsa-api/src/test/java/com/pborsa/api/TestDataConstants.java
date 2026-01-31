package com.pborsa.api;

/**
 * Constants for test data IDs and values.
 * <p>
 * These constants match the IDs used in SQL test data scripts.
 * Using high IDs (9000+) to avoid conflicts with production migrations.
 */
public final class TestDataConstants {

    // Strategy test data (9001-9099 range)
    public static final Long TEST_BASE_STRATEGY_ID = 9001L;
    public static final String TEST_BASE_STRATEGY_CODE = "TEST_MOMENTUM";
    public static final String TEST_BASE_STRATEGY_NAME = "Test Momentum Strategy";

    public static final Long TEST_INACTIVE_STRATEGY_ID = 9002L;
    public static final String TEST_INACTIVE_STRATEGY_CODE = "TEST_INACTIVE";

    public static final Long TEST_MEAN_REVERSION_STRATEGY_ID = 9003L;
    public static final String TEST_MEAN_REVERSION_STRATEGY_CODE = "TEST_MEAN_REVERSION";

    // Strategy test user (9001)
    public static final Long TEST_STRATEGY_USER_ID = 9001L;
    public static final String TEST_STRATEGY_USER_UID = "strategy-test-uid";
    public static final String TEST_STRATEGY_USER_EMAIL = "strategy-test@example.com";

    // User strategy test data
    public static final Long TEST_USER_STRATEGY_ID = 9001L;
    public static final String TEST_USER_STRATEGY_SYMBOL = "AAPL";

    // User test data (9101-9199 range)
    public static final Long TEST_USER_1_ID = 9101L;
    public static final String TEST_USER_1_UID = "user-test-uid-1";
    public static final String TEST_USER_1_EMAIL = "user-test-1@example.com";

    public static final Long TEST_USER_2_ID = 9102L;
    public static final String TEST_USER_2_UID = "user-test-uid-2";
    public static final String TEST_USER_2_EMAIL = "user-test-2@example.com";

    public static final Long TEST_ADMIN_ID = 9103L;
    public static final String TEST_ADMIN_UID = "admin-test-uid";
    public static final String TEST_ADMIN_EMAIL = "admin-test@example.com";

    public static final Long TEST_SUSPENDED_USER_ID = 9104L;
    public static final String TEST_SUSPENDED_USER_UID = "suspended-test-uid";

    // Default test values
    public static final String DEFAULT_TEST_UID = "test-uid";
    public static final String DEFAULT_TEST_EMAIL = "test@example.com";
    public static final String DEFAULT_TEST_DISPLAY_NAME = "Test User";
    public static final String DEFAULT_TEST_PROVIDER = "google.com";

    private TestDataConstants() {
        // Utility class - prevent instantiation
    }
}
