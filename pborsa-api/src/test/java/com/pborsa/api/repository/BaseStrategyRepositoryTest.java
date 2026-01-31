package com.pborsa.api.repository;

import com.pborsa.api.BaseRepositoryTest;
import com.pborsa.api.config.TestCacheConfig;
import com.pborsa.api.domain.entity.BaseStrategyEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Repository tests for {@link BaseStrategyRepository}.
 * <p>
 * Uses @Transactional for automatic rollback after each test.
 * Test data is created within each test method, so no SQL scripts needed.
 */
@Import(TestCacheConfig.class)
@DisplayName("BaseStrategyRepository")
class BaseStrategyRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private BaseStrategyRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    private BaseStrategyEntity createAndPersistStrategy(String code, String name, boolean active) {
        BaseStrategyEntity entity = new BaseStrategyEntity();
        entity.setCode(code);
        entity.setName(name);
        entity.setDescription("Test description for " + name);
        entity.setActive(active);
        return entityManager.persist(entity);
    }

    @Nested
    @DisplayName("findByActiveTrue")
    class FindByActiveTrue {

        @Test
        @DisplayName("should return only active strategies")
        void returnsOnlyActiveStrategies() {
            // given
            createAndPersistStrategy("ACTIVE_1", "Active Strategy 1", true);
            createAndPersistStrategy("ACTIVE_2", "Active Strategy 2", true);
            createAndPersistStrategy("INACTIVE_1", "Inactive Strategy", false);
            entityManager.flush();

            // when
            List<BaseStrategyEntity> result = repository.findByActiveTrue();

            // then - Note: result may include strategies from migrations
            assertThat(result).extracting(BaseStrategyEntity::getCode)
                    .contains("ACTIVE_1", "ACTIVE_2")
                    .doesNotContain("INACTIVE_1");
        }

        @Test
        @DisplayName("should return empty list when no active strategies")
        void returnsEmptyWhenNoActiveStrategies() {
            // given - only inactive strategies
            createAndPersistStrategy("INACTIVE_1", "Inactive 1", false);
            createAndPersistStrategy("INACTIVE_2", "Inactive 2", false);
            entityManager.flush();

            // when
            List<BaseStrategyEntity> result = repository.findByActiveTrue();

            // then - filter out migration strategies for this assertion
            List<String> testCodes = result.stream()
                    .map(BaseStrategyEntity::getCode)
                    .filter(code -> code.startsWith("INACTIVE_"))
                    .toList();

            assertThat(testCodes).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByCode")
    class FindByCode {

        @Test
        @DisplayName("should return strategy when code exists")
        void returnsStrategyWhenCodeExists() {
            // given
            String code = "UNIQUE_TEST_STRATEGY";
            createAndPersistStrategy(code, "Unique Test Strategy", true);
            entityManager.flush();

            // when
            Optional<BaseStrategyEntity> result = repository.findByCode(code);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getCode()).isEqualTo(code);
            assertThat(result.get().getName()).isEqualTo("Unique Test Strategy");
        }

        @Test
        @DisplayName("should return empty when code does not exist")
        void returnsEmptyWhenCodeNotFound() {
            // when
            Optional<BaseStrategyEntity> result = repository.findByCode("NONEXISTENT_CODE");

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should be case sensitive")
        void isCaseSensitive() {
            // given
            createAndPersistStrategy("CASE_TEST", "Case Test Strategy", true);
            entityManager.flush();

            // when
            Optional<BaseStrategyEntity> upperResult = repository.findByCode("CASE_TEST");
            Optional<BaseStrategyEntity> lowerResult = repository.findByCode("case_test");

            // then
            assertThat(upperResult).isPresent();
            assertThat(lowerResult).isEmpty();
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("should return strategy when id exists")
        void returnsStrategyWhenIdExists() {
            // given
            BaseStrategyEntity saved = createAndPersistStrategy("ID_TEST", "ID Test Strategy", true);
            entityManager.flush();
            Long id = saved.getId();

            // when
            Optional<BaseStrategyEntity> result = repository.findById(id);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getCode()).isEqualTo("ID_TEST");
        }

        @Test
        @DisplayName("should return empty when id does not exist")
        void returnsEmptyWhenIdNotFound() {
            // when
            Optional<BaseStrategyEntity> result = repository.findById(999999L);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findAll")
    class FindAll {

        @Test
        @DisplayName("should return all strategies including inactive")
        void returnsAllStrategies() {
            // given
            createAndPersistStrategy("ALL_ACTIVE", "Active Strategy", true);
            createAndPersistStrategy("ALL_INACTIVE", "Inactive Strategy", false);
            entityManager.flush();

            // when
            List<BaseStrategyEntity> result = repository.findAll();

            // then - Note: result includes strategies from migrations
            assertThat(result).extracting(BaseStrategyEntity::getCode)
                    .contains("ALL_ACTIVE", "ALL_INACTIVE");
        }
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("should persist new strategy")
        void persistsNewStrategy() {
            // given
            BaseStrategyEntity entity = new BaseStrategyEntity();
            entity.setCode("NEW_SAVE_TEST");
            entity.setName("Save Test Strategy");
            entity.setDescription("Test save operation");
            entity.setActive(true);

            // when
            BaseStrategyEntity saved = repository.save(entity);
            entityManager.flush();

            // then
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getCreatedAt()).isNotNull();
            assertThat(saved.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("should update existing strategy")
        void updatesExistingStrategy() {
            // given
            BaseStrategyEntity entity = createAndPersistStrategy("UPDATE_TEST", "Original Name", true);
            entityManager.flush();
            Long id = entity.getId();

            // when
            entity.setName("Updated Name");
            repository.save(entity);
            entityManager.flush();
            entityManager.clear();

            // then
            Optional<BaseStrategyEntity> updated = repository.findById(id);
            assertThat(updated).isPresent();
            assertThat(updated.get().getName()).isEqualTo("Updated Name");
        }
    }
}
