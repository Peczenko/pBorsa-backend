package com.pborsa.api;

import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base class for repository/JPA layer tests.
 * <p>
 * Provides:
 * - @DataJpaTest for JPA layer isolation
 * - PostgreSQL Testcontainer with Flyway migrations
 * - Automatic transaction rollback after each test
 * <p>
 * Example usage:
 * <pre>
 * {@code
 * @Transactional  // Each test rolls back automatically
 * class BaseStrategyRepositoryTest extends BaseRepositoryTest {
 *
 *     @Autowired
 *     private BaseStrategyRepository repository;
 *
 *     @Autowired
 *     private TestEntityManager entityManager;
 *
 *     @Test
 *     void findByActiveTrue_returnsOnlyActiveStrategies() {
 *         // given - insert test data within transaction
 *         BaseStrategyEntity active = new BaseStrategyEntity();
 *         active.setCode("ACTIVE_TEST");
 *         active.setActive(true);
 *         entityManager.persist(active);
 *         entityManager.flush();
 *
 *         // when
 *         List<BaseStrategyEntity> result = repository.findByActiveTrue();
 *
 *         // then
 *         assertThat(result).extracting(BaseStrategyEntity::getCode)
 *             .contains("ACTIVE_TEST");
 *     }
 *     // Transaction rolls back - no cleanup needed!
 * }
 * }
 * </pre>
 */
@DataJpaTest
@Testcontainers
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public abstract class BaseRepositoryTest {

    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("pborsa_test")
                    .withUsername("test")
                    .withPassword("test");

    static {
        postgres.start();
    }

    @DynamicPropertySource
    static void datasourceProps(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);
    }
}

