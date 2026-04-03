package com.pborsa.api.security;

import com.pborsa.api.TestDataConstants;
import com.pborsa.api.shared.security.FirebaseUserPrincipal;

/**
 * Utility class for creating test FirebaseUserPrincipal instances.
 * <p>
 * Provides factory methods for creating principals with various configurations
 * for use in controller and service tests.
 */
public final class TestUserPrincipal {

    private TestUserPrincipal() {
        // Utility class - prevent instantiation
    }

    /**
     * Creates a standard test user principal with default values.
     */
    public static FirebaseUserPrincipal standard() {
        return new FirebaseUserPrincipal(
                TestDataConstants.DEFAULT_TEST_UID,
                TestDataConstants.DEFAULT_TEST_EMAIL,
                TestDataConstants.DEFAULT_TEST_DISPLAY_NAME,
                TestDataConstants.DEFAULT_TEST_PROVIDER,
                false
        );
    }

    /**
     * Creates a test user principal with specified uid.
     */
    public static FirebaseUserPrincipal withUid(String uid) {
        return new FirebaseUserPrincipal(
                uid,
                TestDataConstants.DEFAULT_TEST_EMAIL,
                TestDataConstants.DEFAULT_TEST_DISPLAY_NAME,
                TestDataConstants.DEFAULT_TEST_PROVIDER,
                false
        );
    }

    /**
     * Creates a test user principal with specified uid and email.
     */
    public static FirebaseUserPrincipal withUidAndEmail(String uid, String email) {
        return new FirebaseUserPrincipal(
                uid,
                email,
                TestDataConstants.DEFAULT_TEST_DISPLAY_NAME,
                TestDataConstants.DEFAULT_TEST_PROVIDER,
                false
        );
    }

    /**
     * Creates a test admin user principal.
     */
    public static FirebaseUserPrincipal admin() {
        return new FirebaseUserPrincipal(
                TestDataConstants.TEST_ADMIN_UID,
                TestDataConstants.TEST_ADMIN_EMAIL,
                "Test Admin",
                TestDataConstants.DEFAULT_TEST_PROVIDER,
                true
        );
    }

    /**
     * Creates a custom test user principal using the builder pattern.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for creating custom FirebaseUserPrincipal instances.
     */
    public static class Builder {
        private String uid = TestDataConstants.DEFAULT_TEST_UID;
        private String email = TestDataConstants.DEFAULT_TEST_EMAIL;
        private String displayName = TestDataConstants.DEFAULT_TEST_DISPLAY_NAME;
        private String provider = TestDataConstants.DEFAULT_TEST_PROVIDER;
        private boolean admin = false;

        public Builder uid(String uid) {
            this.uid = uid;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public Builder provider(String provider) {
            this.provider = provider;
            return this;
        }

        public Builder admin(boolean admin) {
            this.admin = admin;
            return this;
        }

        public FirebaseUserPrincipal build() {
            return new FirebaseUserPrincipal(uid, email, displayName, provider, admin);
        }
    }
}
