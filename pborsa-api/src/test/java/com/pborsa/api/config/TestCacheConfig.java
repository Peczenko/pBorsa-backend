package com.pborsa.api.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

/**
 * Minimal test configuration that provides no-op CacheManagers.
 * <p>
 * Use this for tests that don't need full security configuration
 * (like @DataJpaTest) but still need CacheManagers due to @EnableCaching.
 * Provides all named cache managers required by services.
 */
@TestConfiguration
@Profile("test")
public class TestCacheConfig {

    /**
     * Creates a no-op CacheManager for testing.
     * This satisfies the @EnableCaching requirement without actual caching.
     * Marked as @Primary to resolve ambiguity when multiple CacheManagers are present.
     */
    @Bean
    @Primary
    @ConditionalOnMissingBean
    public CacheManager cacheManager() {
        return new NoOpCacheManager();
    }

    @Bean("apiCredentialsCacheManager")
    @ConditionalOnMissingBean(name = "apiCredentialsCacheManager")
    public CacheManager apiCredentialsCacheManager() {
        return new NoOpCacheManager();
    }

    @Bean("marketDataCacheManager")
    @ConditionalOnMissingBean(name = "marketDataCacheManager")
    public CacheManager marketDataCacheManager() {
        return new NoOpCacheManager();
    }

    @Bean("positionsCacheManager")
    @ConditionalOnMissingBean(name = "positionsCacheManager")
    public CacheManager positionsCacheManager() {
        return new NoOpCacheManager();
    }

    @Bean("accountInfoCacheManager")
    @ConditionalOnMissingBean(name = "accountInfoCacheManager")
    public CacheManager accountInfoCacheManager() {
        return new NoOpCacheManager();
    }
}
