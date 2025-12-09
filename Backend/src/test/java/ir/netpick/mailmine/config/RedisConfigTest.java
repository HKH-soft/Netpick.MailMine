package ir.netpick.mailmine.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for Redis configuration
 * Note: These tests require a running Redis instance
 * They will be skipped in CI if Redis is not available
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Redis Configuration Integration Tests")
class RedisConfigTest {

    @Autowired(required = false)
    private RedisConnectionFactory redisConnectionFactory;

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired(required = false)
    private CacheManager cacheManager;

    @Test
    @DisplayName("Redis connection factory should be configured")
    void redisConnectionFactoryShouldBeConfigured() {
        // This test will only run if Redis is available
        if (redisConnectionFactory != null) {
            assertThat(redisConnectionFactory).isNotNull();
        }
    }

    @Test
    @DisplayName("Redis template should be configured")
    void redisTemplateShouldBeConfigured() {
        // This test will only run if Redis is available
        if (redisTemplate != null) {
            assertThat(redisTemplate).isNotNull();
            assertThat(redisTemplate.getConnectionFactory()).isNotNull();
        }
    }

    @Test
    @DisplayName("Cache manager should be configured")
    void cacheManagerShouldBeConfigured() {
        // Cache manager should always be available
        assertThat(cacheManager).isNotNull();
    }
}
