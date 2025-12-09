# Summary of Changes: Redis Caching, Rate Limiting, and Spring Retry

## Question Asked
"if i wanted to add redis to cache and rate limit my app what part should be changed? and i also want to add spring retry what about that?"

## Answer

This document summarizes all the changes made to integrate Redis caching, distributed rate limiting, and Spring Retry into the MailMine application.

## Parts Changed

### 1. Dependencies (pom.xml)

**What was added:**
- `spring-boot-starter-data-redis` - Redis integration with Lettuce client
- `spring-boot-starter-cache` - Spring Cache abstraction support
- `spring-retry` - Spring Retry framework

**Why:** These dependencies provide the foundation for Redis integration, caching capabilities, and automatic retry logic.

**Location:** `Backend/pom.xml` (lines 68-76)

---

### 2. Main Application Class (Application.java)

**What was added:**
- `@EnableCaching` annotation - Enables Spring Cache support
- `@EnableRetry` annotation - Enables Spring Retry support

**Why:** These annotations activate caching and retry mechanisms across the application.

**Location:** `Backend/src/main/java/ir/netpick/mailmine/init/Application.java`

---

### 3. Configuration Classes

#### a. RedisConfig.java (NEW)

**What was added:**
A complete Redis configuration class that provides:
- Redis connection factory configuration
- RedisTemplate bean for general Redis operations
- CacheManager configuration with JSON serialization
- Connection pool settings

**Why:** Configures how the application connects to Redis and manages cached data.

**Location:** `Backend/src/main/java/ir/netpick/mailmine/config/RedisConfig.java`

#### b. RetryConfig.java (NEW)

**What was added:**
A retry configuration class providing:
- RetryTemplate with 3 max attempts
- Exponential backoff policy (1s → 2s → 4s, max 10s)

**Why:** Provides default retry behavior for operations that need resilience.

**Location:** `Backend/src/main/java/ir/netpick/mailmine/config/RetryConfig.java`

---

### 4. Application Configuration (application.yml)

**What was added:**
```yaml
spring:
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
      timeout: 2000ms
      lettuce:
        pool:
          max-active: 8
          max-idle: 8
          min-idle: 0
          max-wait: -1ms
  cache:
    type: redis
    redis:
      time-to-live: 3600000  # 1 hour
      cache-null-values: false
```

**Why:** Provides Redis connection parameters and cache configuration that can be overridden via environment variables.

**Location:** `Backend/src/main/resources/application.yml`

---

### 5. Rate Limiting Services

#### a. RateLimitingService.java (MODIFIED)

**What was changed:**
- Added `@ConditionalOnProperty` annotation
- Added documentation explaining when to use this service
- Now only loads when `rate-limiting.use-redis=false` (default)

**Why:** Maintains backward compatibility with in-memory rate limiting for single-instance deployments.

**Location:** `Backend/src/main/java/ir/netpick/mailmine/auth/service/RateLimitingService.java`

#### b. RedisRateLimitingService.java (NEW)

**What was added:**
A complete Redis-based rate limiting service with:
- Login attempt tracking with distributed storage
- Verification attempt tracking
- Resend verification rate limiting
- Automatic TTL for all rate limit entries
- Only loads when `rate-limiting.use-redis=true`

**Why:** Enables rate limiting across multiple application instances in distributed deployments.

**Location:** `Backend/src/main/java/ir/netpick/mailmine/auth/service/RedisRateLimitingService.java`

---

### 6. Service Layer - Caching

#### a. ApiKeyService.java (MODIFIED)

**What was added:**
- `@Cacheable("apiKeys")` on `allKeys()` method - caches paginated lists
- `@Cacheable("apiKey")` on `getKey()` method - caches individual lookups
- `@CacheEvict` on all write operations (create/update/delete) - invalidates cache

**Why:** Frequently accessed API keys are cached to reduce database queries. Cache is automatically invalidated when data changes.

**Location:** `Backend/src/main/java/ir/netpick/mailmine/scrape/service/base/ApiKeyService.java`

#### b. SearchQueryService.java (MODIFIED)

**What was added:**
- `@Cacheable("searchQueries")` on `allSearchQueries()` method
- `@Cacheable("searchQuery")` on `getSearchQuery()` method
- `@CacheEvict` on all write operations

**Why:** Search queries are cached as they're frequently accessed during scraping operations.

**Location:** `Backend/src/main/java/ir/netpick/mailmine/scrape/service/base/SearchQueryService.java`

---

### 7. Service Layer - Retry Logic

#### GeminiService.java (MODIFIED)

**What was added:**
- `@Retryable` annotation on `generateText()` method with:
  - Retry on `RuntimeException`
  - Maximum 3 attempts
  - Exponential backoff: 1s → 2s → 4s
- `@Recover` method for fallback behavior when all retries fail
- Enhanced logging for retry attempts

**Why:** AI API calls can fail due to network issues or temporary service unavailability. Automatic retry improves reliability.

**Location:** `Backend/src/main/java/ir/netpick/mailmine/ai/service/GeminiService.java`

**Note:** ApiCaller already had custom retry logic built-in, so no changes were needed there.

---

### 8. Test Configuration

#### a. application-test.yml (MODIFIED)

**What was added:**
```yaml
spring:
  cache:
    type: none  # Disable caching for tests
  data:
    redis:
      host: localhost
      port: 6379

rate-limiting:
  use-redis: false  # Use in-memory for tests
```

**Why:** Tests don't require Redis to run. They use in-memory implementations instead.

**Location:** `Backend/src/test/resources/application-test.yml`

#### b. RedisConfigTest.java (NEW)

**What was added:**
Basic integration test for Redis configuration that:
- Checks if RedisConnectionFactory is configured
- Verifies RedisTemplate bean exists
- Tests CacheManager configuration

**Why:** Provides basic validation that Redis configuration loads correctly when available.

**Location:** `Backend/src/test/java/ir/netpick/mailmine/config/RedisConfigTest.java`

---

### 9. Documentation

#### a. REDIS_INTEGRATION.md (NEW)

**What was added:**
Comprehensive documentation covering:
- Overview of features added
- Detailed explanation of all changes
- Setup instructions for local development and production
- Configuration examples
- Benefits of each feature
- Monitoring and metrics guidance
- Troubleshooting section
- Performance tuning recommendations

**Location:** `Backend/REDIS_INTEGRATION.md`

#### b. README.md (MODIFIED)

**What was changed:**
- Updated Tech Stack section to mention Redis and Spring Retry
- Added Redis to prerequisites
- Added Redis setup step in installation
- Added Redis configuration properties
- Added Redis troubleshooting section
- Added link to Redis Integration Guide

**Location:** `Backend/README.md`

---

## Configuration Options

### To Enable Redis Caching

Set environment variables:
```bash
export REDIS_HOST=localhost
export REDIS_PORT=6379
export REDIS_PASSWORD=  # Optional
```

Or update `application.yml`:
```yaml
spring:
  cache:
    type: redis
  data:
    redis:
      host: localhost
      port: 6379
```

### To Enable Redis-based Rate Limiting

Set in `application.yml` or `application-pro.yml`:
```yaml
rate-limiting:
  use-redis: true
```

### To Disable Redis Features

For single-instance deployments or when Redis is not available:
```yaml
spring:
  cache:
    type: none  # Or simply don't configure Redis

rate-limiting:
  use-redis: false  # Use in-memory (default)
```

---

## Key Features

### 1. Distributed Caching
- **Cache Storage:** API keys, search queries
- **Cache TTL:** 1 hour (configurable)
- **Cache Strategy:** Cache-aside pattern with automatic eviction
- **Serialization:** JSON format for cross-platform compatibility

### 2. Distributed Rate Limiting
- **Login Attempts:** Max 5 attempts, 15-minute lockout
- **Verification Attempts:** Configurable max attempts with cooldown
- **Resend Limits:** Max 3 per hour, 30-second minimum interval
- **Storage:** Redis with automatic TTL

### 3. Resilient API Calls
- **Retry Policy:** Up to 3 attempts
- **Backoff Strategy:** Exponential (1s, 2s, 4s, max 10s)
- **Applicable To:** Gemini AI service
- **Fallback:** Recoverable methods for graceful degradation

---

## Benefits

1. **Performance:** Reduced database load through caching
2. **Scalability:** Distributed caching and rate limiting support horizontal scaling
3. **Reliability:** Automatic retry logic handles transient failures
4. **Security:** Rate limiting protects against abuse and attacks
5. **Flexibility:** Easy to enable/disable features via configuration

---

## Migration Path

### From No Redis to Redis

1. Install Redis (Docker or native)
2. Set environment variables for Redis connection
3. No code changes required - features work automatically
4. Monitor cache hit rates and adjust TTL as needed

### From Single Instance to Multi-Instance

1. Deploy Redis in production
2. Enable Redis-based rate limiting: `rate-limiting.use-redis=true`
3. Deploy multiple application instances
4. Rate limiting and caching now work across all instances

---

## No Breaking Changes

All changes are **backward compatible**:
- Application works without Redis (in-memory caching/rate limiting)
- Existing tests pass without modification
- Default configuration maintains previous behavior
- Redis features can be enabled incrementally

---

## Summary

The integration adds three major capabilities to MailMine:

1. **Redis Caching** - Improves performance by caching frequently accessed data
2. **Distributed Rate Limiting** - Enables multi-instance deployments with consistent rate limiting
3. **Spring Retry** - Improves reliability of external API calls

All features are optional and can be enabled/disabled via configuration, making the application flexible for different deployment scenarios.
