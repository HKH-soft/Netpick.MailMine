# Redis Caching and Rate Limiting Integration

This document explains the Redis caching, rate limiting, and Spring Retry integration added to the MailMine application.

## Overview

The following features have been added:
1. **Redis Caching**: Distributed caching for frequently accessed data
2. **Redis-based Rate Limiting**: Distributed rate limiting for multi-instance deployments
3. **Spring Retry**: Automatic retry logic for external API calls

## Changes Made

### 1. Dependencies Added (pom.xml)

- `spring-boot-starter-data-redis`: Redis integration with Lettuce client
- `spring-boot-starter-cache`: Spring Cache abstraction
- `spring-retry`: Spring Retry support

### 2. Configuration

#### Redis Configuration (RedisConfig.java)

Location: `src/main/java/ir/netpick/mailmine/config/RedisConfig.java`

Configures:
- Redis connection factory with Lettuce client
- RedisTemplate for general Redis operations
- CacheManager with JSON serialization

#### Retry Configuration (RetryConfig.java)

Location: `src/main/java/ir/netpick/mailmine/config/RetryConfig.java`

Configures:
- Default retry template with 3 max attempts
- Exponential backoff (initial: 1s, multiplier: 2x, max: 10s)

#### Application Configuration (application.yml)

New properties added:
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
      time-to-live: 3600000  # 1 hour in milliseconds
      cache-null-values: false
```

#### Rate Limiting Configuration

New property to control rate limiting implementation:
```yaml
rate-limiting:
  use-redis: false  # Set to true for distributed deployments
```

### 3. Rate Limiting Services

#### In-Memory Rate Limiting (Default)

Location: `src/main/java/ir/netpick/mailmine/auth/service/RateLimitingService.java`

- Uses `ConcurrentHashMap` for single-instance deployments
- Active when `rate-limiting.use-redis=false` (default)
- No external dependencies required

#### Redis-based Rate Limiting

Location: `src/main/java/ir/netpick/mailmine/auth/service/RedisRateLimitingService.java`

- Uses Redis for distributed rate limiting
- Active when `rate-limiting.use-redis=true`
- Required for multi-instance deployments
- Stores rate limit data with TTL in Redis

Both services provide the same API:
- `canAttemptLogin(email)`
- `recordFailedLoginAttempt(email)`
- `clearLoginAttempts(email)`
- `canAttemptVerification(email)`
- `recordVerificationAttempt(email)`
- `canResendVerification(email)`
- `recordResendAttempt(email)`

### 4. Caching Implementation

#### ApiKeyService

Cached operations:
- `@Cacheable("apiKeys")`: Cache paginated API keys list
- `@Cacheable("apiKey")`: Cache individual API key lookups
- `@CacheEvict`: Clear cache on create/update/delete operations

#### SearchQueryService

Cached operations:
- `@Cacheable("searchQueries")`: Cache paginated search queries
- `@Cacheable("searchQuery")`: Cache individual query lookups
- `@CacheEvict`: Clear cache on create/update/delete operations

### 5. Retry Logic

#### GeminiService

Added retry logic for AI API calls:
- `@Retryable`: Retry up to 3 times on `RuntimeException`
- Exponential backoff: 1s → 2s → 4s (max 10s)
- `@Recover`: Fallback method after all retries exhausted

Example:
```java
@Retryable(
    retryFor = { RuntimeException.class },
    maxAttempts = 3,
    backoff = @Backoff(delay = 1000, multiplier = 2.0, maxDelay = 10000),
    recover = "recoverGenerateText"
)
public String generateText(String prompt) {
    // API call logic
}
```

## Setup Instructions

### Local Development (Single Instance)

1. No additional setup required
2. Default in-memory rate limiting is used
3. Caching is disabled by default without Redis

### Production (With Redis)

1. **Install Redis**:
   ```bash
   # Docker
   docker run -d -p 6379:6379 --name redis redis:7-alpine
   
   # Or use docker-compose (recommended)
   # Add to your docker-compose.yml:
   redis:
     image: redis:7-alpine
     ports:
       - "6379:6379"
     volumes:
       - redis-data:/data
   ```

2. **Configure Environment Variables**:
   ```bash
   export REDIS_HOST=localhost
   export REDIS_PORT=6379
   export REDIS_PASSWORD=  # Leave empty if no password
   ```

3. **Enable Redis Rate Limiting** (optional for distributed deployments):
   Add to `application-pro.yml`:
   ```yaml
   rate-limiting:
     use-redis: true
   ```

4. **Run the Application**:
   ```bash
   ./mvnw spring-boot:run
   ```

### Testing Redis Connection

You can verify Redis connectivity:
```bash
redis-cli ping
# Should return: PONG
```

## Benefits

### Caching
- **Reduced Database Load**: Frequently accessed data served from cache
- **Improved Response Time**: Cache hits are much faster than database queries
- **Scalability**: Distributed cache shared across multiple instances

### Rate Limiting
- **Distributed Rate Limiting**: Works across multiple application instances
- **Attack Prevention**: Protects against brute-force and abuse
- **Flexible Configuration**: Easy to switch between in-memory and Redis-based

### Retry Logic
- **Resilience**: Automatic retry on transient failures
- **Better Reliability**: Handles temporary network issues gracefully
- **Configurable Backoff**: Exponential backoff prevents overwhelming services

## Monitoring

### Redis Metrics

Redis operations are automatically tracked by Spring Boot Actuator (if enabled):
- Cache hit/miss ratio
- Redis connection pool metrics
- Operation latencies

Access metrics at: `http://localhost:8080/actuator/metrics`

### Cache Statistics

Key cache metrics:
- `cache.gets`: Total cache get operations
- `cache.puts`: Total cache put operations
- `cache.evictions`: Cache evictions count
- `cache.size`: Current cache size

## Troubleshooting

### Redis Connection Issues

If you see connection errors:
1. Verify Redis is running: `redis-cli ping`
2. Check connection settings in environment variables
3. Verify network connectivity to Redis host
4. Check Redis logs for authentication failures

### Cache Not Working

If caching doesn't seem to work:
1. Verify `@EnableCaching` is present in `Application.java`
2. Check Redis connection is successful
3. Ensure cacheable methods are called from outside the class (Spring proxy requirement)
4. Review cache TTL settings

### Rate Limiting Issues

If rate limiting doesn't work as expected:
1. Check which service is active (in-memory vs Redis)
2. For Redis-based: verify Redis connection
3. Clear Redis keys manually if needed: `redis-cli FLUSHALL`
4. Check logs for rate limit events

## Performance Tuning

### Cache TTL

Adjust cache time-to-live in `application.yml`:
```yaml
spring:
  cache:
    redis:
      time-to-live: 1800000  # 30 minutes
```

### Redis Connection Pool

Tune connection pool settings:
```yaml
spring:
  data:
    redis:
      lettuce:
        pool:
          max-active: 16  # Increase for high concurrency
          max-idle: 8
          min-idle: 2
```

### Retry Configuration

Customize retry behavior in specific services:
```java
@Retryable(
    maxAttempts = 5,  // More attempts
    backoff = @Backoff(delay = 2000, multiplier = 1.5)
)
```

## Further Enhancements

Potential improvements:
1. Add cache warming on application startup
2. Implement cache eviction strategies per entity type
3. Add distributed locks for critical operations
4. Implement circuit breaker pattern with Resilience4j
5. Add more comprehensive retry policies for different failure types
