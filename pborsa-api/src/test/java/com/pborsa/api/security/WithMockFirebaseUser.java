package com.pborsa.api.security;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom annotation for setting up mock Firebase authentication in tests.
 * <p>
 * Use this annotation on test methods or classes to simulate an authenticated
 * Firebase user in the security context.
 * <p>
 * Example usage:
 * <pre>
 * {@code
 * @Test
 * @WithMockFirebaseUser(uid = "test-uid", email = "test@example.com")
 * void myTest() {
 *     // Test runs with authenticated user
 * }
 *
 * @Test
 * @WithMockFirebaseUser(admin = true)
 * void adminTest() {
 *     // Test runs with admin user
 * }
 * }
 * </pre>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockFirebaseUserSecurityContextFactory.class)
public @interface WithMockFirebaseUser {

    /**
     * The Firebase UID for the mock user.
     */
    String uid() default "test-uid";

    /**
     * The email address for the mock user.
     */
    String email() default "test@example.com";

    /**
     * The display name for the mock user.
     */
    String displayName() default "Test User";

    /**
     * The authentication provider (e.g., "google.com", "password").
     */
    String provider() default "google.com";

    /**
     * Whether the mock user has admin privileges.
     */
    boolean admin() default false;
}
