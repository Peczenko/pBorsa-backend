package com.pborsa.api.shared.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * JPA configuration for repository scanning across multiple packages.
 * <p>
 * This is separated from the main application class so that {@code @WebMvcTest}
 * slices do not attempt to initialize JPA infrastructure (EntityManagerFactory,
 * DataSource) which are not available in web-only test contexts.
 * <p>
 * {@code @DataJpaTest} and {@code @SpringBootTest} will pick this up through
 * normal component scanning since it resides within the {@code com.pborsa.api}
 * package hierarchy.
 */
@Configuration
@EnableJpaRepositories(basePackages = {"com.pborsa.api", "com.pborsa.trading"})
public class JpaConfig {
}
