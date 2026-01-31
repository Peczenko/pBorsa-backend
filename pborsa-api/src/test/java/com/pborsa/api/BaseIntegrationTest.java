package com.pborsa.api;

import com.pborsa.api.config.TestSecurityConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base class for full integration tests.
 * <p>
 * Provides:
 * - SpringBootTest with random port
 * - Test profile activation (disables Firebase, Temporal, gRPC)
 * - PostgreSQL Testcontainer with Flyway migrations
 * - TestRestTemplate for HTTP requests
 * - Disabled security via TestSecurityConfig
 * <p>
 * Example usage:
 * <pre>
 * {@code
 * @Sql(scripts = "/sql/user/init.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
 * @Sql(scripts = "/sql/user/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_CLASS)
 * class UserControllerIntegrationTest extends BaseIntegrationTest {
 *     @Test
 *     void getCurrentUser_returnsUserProfile() {
 *         ResponseEntity<String> response = restTemplate.getForEntity("/api/v1/users/me", String.class);
 *         assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
 *     }
 * }
 * }
 * </pre>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
@Import(TestSecurityConfig.class)
@AutoConfigureTestRestTemplate
public abstract class BaseIntegrationTest {

    /**
     * Shared PostgreSQL container for all integration tests.
     * Uses @ServiceConnection for automatic datasource configuration.
     */
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("pborsa_test")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    protected TestRestTemplate restTemplate;
}
