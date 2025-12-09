# Backend Code Issues and Problems

This document lists all identified problems and potential issues in the MailMine backend modules that should be fixed later.

## Table of Contents
1. [AI Module Issues](#ai-module-issues)
2. [Authentication Module Issues](#authentication-module-issues)
3. [Email Module Issues](#email-module-issues)
4. [Scrape Module Issues](#scrape-module-issues)
5. [Common Module Issues](#common-module-issues)
6. [Configuration Issues](#configuration-issues)
7. [Security Issues](#security-issues)

---

## AI Module Issues

### GeminiService.java
**File**: `src/main/java/ir/netpick/mailmine/ai/service/GeminiService.java`

1. **Missing API Key Configuration**
   - **Issue**: The Gemini service doesn't explicitly configure or validate the API key
   - **Problem**: Service will fail at runtime if GOOGLE_API_KEY is not set
   - **Severity**: HIGH
   - **Line**: 22-37

2. **No Rate Limiting**
   - **Issue**: No rate limiting for Gemini API calls
   - **Problem**: Could exceed API quotas quickly, leading to service disruption
   - **Severity**: MEDIUM
   - **Line**: 21-38

3. **No Timeout Configuration**
   - **Issue**: API calls don't have explicit timeout configuration
   - **Problem**: Hanging requests could block threads indefinitely
   - **Severity**: MEDIUM
   - **Line**: 22-24

4. **Generic Exception Handling**
   - **Issue**: Catches all exceptions and wraps them as RuntimeException
   - **Problem**: Loses specific error information, makes debugging harder
   - **Severity**: LOW
   - **Line**: 25-37

### GeminiController.java
**File**: `src/main/java/ir/netpick/mailmine/ai/controller/GeminiController.java`

5. **No Input Validation**
   - **Issue**: No maximum length check on prompt input
   - **Problem**: Users could submit extremely long prompts, causing performance issues
   - **Severity**: MEDIUM
   - **Line**: 18-25

6. **No Authentication/Authorization**
   - **Issue**: AI endpoints appear to be authenticated but no role-based access control
   - **Problem**: All authenticated users can consume expensive AI resources
   - **Severity**: HIGH
   - **Line**: 10-36

7. **Missing Request/Response DTOs**
   - **Issue**: Using raw Map<String, String> instead of proper DTOs
   - **Problem**: No type safety, validation, or API documentation
   - **Severity**: MEDIUM
   - **Line**: 18-35

---

## Authentication Module Issues

### AuthenticationService.java
**File**: `src/main/java/ir/netpick/mailmine/auth/service/AuthenticationService.java`

8. **In-Memory Rate Limiting**
   - **Issue**: RateLimitingService uses ConcurrentHashMap for storing rate limit data
   - **Problem**: Data is lost on application restart, doesn't work in distributed systems
   - **Severity**: HIGH
   - **Line**: Ref to RateLimitingService

9. **Password Policy Not Enforced**
   - **Issue**: No password complexity requirements visible in code
   - **Problem**: Weak passwords could be accepted
   - **Severity**: MEDIUM
   - **Line**: 185-202

10. **No Account Lockout on Verification Failures**
    - **Issue**: Only rate limiting on verification, no permanent lockout
    - **Problem**: Brute force attacks still possible over time
    - **Severity**: MEDIUM
    - **Line**: 219-278

### RateLimitingService.java
**File**: `src/main/java/ir/netpick/mailmine/auth/service/RateLimitingService.java`

11. **Memory Leak Risk**
    - **Issue**: ConcurrentHashMaps never expire old entries
    - **Problem**: Memory usage grows indefinitely with unique email addresses
    - **Severity**: HIGH
    - **Line**: 20-26

12. **No Persistence**
    - **Issue**: All rate limit data is in-memory only
    - **Problem**: Data lost on restart, allows bypass by restarting service
    - **Severity**: HIGH
    - **Line**: 20-26

13. **Hard-Coded Constants**
    - **Issue**: Rate limit values are hard-coded
    - **Problem**: Cannot be adjusted without code changes and redeployment
    - **Severity**: LOW
    - **Line**: 16-31

### JWTUtil.java
**File**: `src/main/java/ir/netpick/mailmine/auth/jwt/JWTUtil.java`

14. **Weak Secret Key Handling**
    - **Issue**: Secret key loaded from config file as string
    - **Problem**: Key might be too short or weak, risk of exposure in config
    - **Severity**: HIGH
    - **Line**: 22-23

15. **No Key Rotation**
    - **Issue**: JWT signing key is static
    - **Problem**: If compromised, all tokens remain valid until expiration
    - **Severity**: MEDIUM
    - **Line**: 84-86

16. **Short Default Token Expiration**
    - **Issue**: Default 15-minute expiration for access tokens
    - **Problem**: May cause poor UX with frequent re-authentication needs
    - **Severity**: LOW
    - **Line**: 25-26

### RefreshTokenService (Referenced)
**Potential Issues**:

17. **Token Storage**
    - **Issue**: Need to verify if refresh tokens are properly hashed in database
    - **Problem**: If stored in plaintext, database compromise exposes all sessions
    - **Severity**: CRITICAL
    - **Action**: Review RefreshTokenService implementation

---

## Email Module Issues

### EmailServiceImpl.java
**File**: `src/main/java/ir/netpick/mailmine/email/service/EmailServiceImpl.java`

18. **No Email Queue/Retry Mechanism**
    - **Issue**: Failed emails throw RuntimeException with no retry
    - **Problem**: Transient failures cause permanent email delivery failure
    - **Severity**: MEDIUM
    - **Line**: 46-49, 72-75, 100-103

19. **Async Methods Without Error Handling**
    - **Issue**: @Async methods throw RuntimeException
    - **Problem**: Exceptions in async methods are swallowed, no notification to user
    - **Severity**: HIGH
    - **Line**: 53-76, 78-104, 119-151

20. **Thread.sleep() in Mass Email**
    - **Issue**: Using Thread.sleep(100) for rate limiting
    - **Problem**: Inefficient, blocks thread pool, and is not configurable
    - **Severity**: MEDIUM
    - **Line**: 143

21. **No Email Validation**
    - **Issue**: No validation that recipient email addresses are properly formatted
    - **Problem**: Could attempt to send to invalid addresses
    - **Severity**: LOW
    - **Line**: 34-50

22. **Mass Email Not Transactional**
    - **Issue**: Mass email method processes all recipients in one async call
    - **Problem**: If service crashes mid-process, no way to resume
    - **Severity**: MEDIUM
    - **Line**: 119-151

23. **Hardcoded Attachment Path**
    - **Issue**: Attachment path taken directly from request without validation
    - **Problem**: Path traversal vulnerability risk
    - **Severity**: HIGH
    - **Line**: 66

---

## Scrape Module Issues

### Scraper.java
**File**: `src/main/java/ir/netpick/mailmine/scrape/service/mid/Scraper.java`

24. **Playwright Instance per Job**
    - **Issue**: Creates new Playwright instance for all jobs
    - **Problem**: Resource intensive, should reuse instance
    - **Severity**: MEDIUM
    - **Line**: 83

25. **Hardcoded Browser Arguments**
    - **Issue**: Browser launch arguments are hardcoded
    - **Problem**: Cannot adjust for different environments
    - **Severity**: LOW
    - **Line**: 156-159

26. **No Proxy Health Check**
    - **Issue**: Proxies are used without pre-checking if they work
    - **Problem**: Wastes time trying dead proxies
    - **Severity**: MEDIUM
    - **Line**: 137-168

27. **User Agent Hardcoded**
    - **Issue**: User agent is hardcoded to Chrome 120
    - **Problem**: Becomes outdated, easy to detect as bot
    - **Severity**: LOW
    - **Line**: 175

28. **Blocked Domains List is Static**
    - **Issue**: Blocked domains are in ScrapeConstants
    - **Problem**: Cannot be updated without code deployment
    - **Severity**: LOW
    - **Line**: 122-133

29. **No Concurrent Scraping**
    - **Issue**: Jobs are processed sequentially
    - **Problem**: Very slow for large job queues
    - **Severity**: HIGH
    - **Line**: 70-109

30. **Page Load Timeout Not Configurable Per Job**
    - **Issue**: Same timeout for all jobs
    - **Problem**: Some sites need more time, others can be faster
    - **Severity**: LOW
    - **Line**: 180-187

### DataProcessor.java
**File**: `src/main/java/ir/netpick/mailmine/scrape/service/mid/DataProcessor.java`

31. **File Read Returns Null**
    - **Issue**: Silently marks data as parsed when file read fails
    - **Problem**: Loses potential data, no alert that files are missing
    - **Severity**: MEDIUM
    - **Line**: 104-111

32. **No Batch Transaction Management**
    - **Issue**: Each file processed in separate transaction
    - **Problem**: Overhead from many small transactions
    - **Severity**: LOW
    - **Line**: 95

33. **Contact Parser Can Return Null**
    - **Issue**: ContactInfoParser.parse() can return null
    - **Problem**: Should use Optional instead for better handling
    - **Severity**: LOW
    - **Line**: 113

### SearchQueryGenerator.java
**File**: `src/main/java/ir/netpick/mailmine/scrape/service/mid/SearchQueryGenerator.java`

34. **No Caching of Generated Queries**
    - **Issue**: Each generation call hits Gemini API
    - **Problem**: Expensive, slow, could hit rate limits
    - **Severity**: MEDIUM
    - **Line**: 44-66

35. **System Instruction Embedded in Code**
    - **Issue**: AI prompt instructions are hardcoded
    - **Problem**: Cannot be tuned without redeployment
    - **Severity**: LOW
    - **Line**: 24-34

36. **No Duplicate Query Detection**
    - **Issue**: Generated queries might be duplicates
    - **Problem**: Wastes scraping resources on duplicate searches
    - **Severity**: MEDIUM
    - **Line**: 76-95

### ApiCaller.java
**File**: `src/main/java/ir/netpick/mailmine/scrape/service/mid/ApiCaller.java`

37. **API Keys Rotated Randomly**
    - **Issue**: Uses ThreadLocalRandom for key selection
    - **Problem**: No tracking of which keys are rate-limited or exhausted
    - **Severity**: MEDIUM
    - **Line**: 128

38. **No API Key Health Tracking**
    - **Issue**: Doesn't track which API keys are working/failing
    - **Problem**: Keeps retrying dead keys
    - **Severity**: MEDIUM
    - **Line**: 128-199

39. **Blocking WebClient Usage**
    - **Issue**: Uses .block() on reactive WebClient
    - **Problem**: Defeats purpose of reactive programming, can cause deadlocks
    - **Severity**: MEDIUM
    - **Line**: 149

40. **Rate Limit Backoff Too Aggressive**
    - **Issue**: Starts with 3 second backoff, doubles each time
    - **Problem**: Could wait very long times unnecessarily
    - **Severity**: LOW
    - **Line**: 55-61

41. **No Circuit Breaker**
    - **Issue**: Keeps calling failing APIs indefinitely
    - **Problem**: Wastes resources on failing services
    - **Severity**: MEDIUM
    - **Line**: 178-220

### V2RayClientService.java
**File**: `src/main/java/ir/netpick/mailmine/scrape/service/base/V2RayClientService.java`

42. **Process Management Issues**
    - **Issue**: V2Ray processes managed manually with maps
    - **Problem**: Risk of zombie processes, resource leaks
    - **Severity**: HIGH
    - **Line**: 45-52

43. **No Process Output Logging Limits**
    - **Issue**: Logs all process output indefinitely
    - **Problem**: Can fill disk with logs from verbose processes
    - **Severity**: MEDIUM
    - **Line**: 149-172

44. **Port Allocation Race Condition**
    - **Issue**: Port availability check, then allocation is not atomic
    - **Problem**: Two processes could allocate same port
    - **Severity**: MEDIUM
    - **Line**: 243-259

45. **Config Files Not Cleaned on Error**
    - **Issue**: If startup fails, config file may remain
    - **Problem**: Disk space leak over time
    - **Severity**: LOW
    - **Line**: 87-144

46. **Hardcoded Timeouts**
    - **Issue**: Wait times for process startup are hardcoded
    - **Problem**: May not work on slower systems
    - **Severity**: LOW
    - **Line**: 117, 126

47. **No Health Monitoring**
    - **Issue**: Doesn't periodically check if V2Ray processes are still alive
    - **Problem**: Dead processes not detected until next use
    - **Severity**: MEDIUM
    - **Line**: 230-233

### ScrapeOrchestrationService.java
**File**: `src/main/java/ir/netpick/mailmine/scrape/service/orch/ScrapeOrchestrationService.java`

48. **No Pipeline Status Webhooks**
    - **Issue**: Pipeline status changes not notified externally
    - **Problem**: Frontend must poll for status updates
    - **Severity**: LOW
    - **Line**: 39-100

49. **No Pipeline Timeout**
    - **Issue**: Pipelines can run indefinitely
    - **Problem**: Stuck pipelines waste resources
    - **Severity**: MEDIUM
    - **Line**: 39-100

---

## Common Module Issues

### DefaultExceptionHandler.java
**File**: `src/main/java/ir/netpick/mailmine/common/exception/DefaultExceptionHandler.java`

50. **Generic Exception Handler Too Broad**
    - **Issue**: Catches all Exception types at the end
    - **Problem**: Masks unexpected errors, returns generic message
    - **Severity**: MEDIUM
    - **Line**: 208-217

51. **No Exception Logging**
    - **Issue**: Exceptions are caught but not logged
    - **Problem**: Errors occur silently, debugging is difficult
    - **Severity**: HIGH
    - **Line**: All handlers

52. **Exposes Internal Error Details**
    - **Issue**: e.getMessage() returned directly to client
    - **Problem**: Could expose sensitive internal information
    - **Severity**: MEDIUM
    - **Line**: Multiple handlers

### Result Pattern
**Files**: `src/main/java/ir/netpick/mailmine/common/result/`

53. **Inconsistent Error Handling**
    - **Issue**: Some services throw exceptions, others return Result objects
    - **Problem**: Inconsistent error handling across codebase
    - **Severity**: LOW
    - **Action**: Review and standardize

---

## Configuration Issues

### OpenApiConfig.java
**File**: `src/main/java/ir/netpick/mailmine/config/OpenApiConfig.java`

54. **Hardcoded Server URL**
    - **Issue**: Only localhost server configured
    - **Problem**: Swagger won't work with deployed instances
    - **Severity**: LOW
    - **Line**: 27-30

### SecurityFilterChainConfig.java
**File**: `src/main/java/ir/netpick/mailmine/auth/security/SecurityFilterChainConfig.java`

55. **Actuator Endpoints Publicly Accessible**
    - **Issue**: /actuator/** endpoints are permitAll()
    - **Problem**: Sensitive metrics and health info exposed to public
    - **Severity**: HIGH
    - **Line**: 44-45

56. **No Request Rate Limiting at Gateway Level**
    - **Issue**: Only application-level rate limiting exists
    - **Problem**: DDoS attacks could overwhelm the service
    - **Severity**: HIGH
    - **Action**: Consider adding Spring Security rate limiting

### CORS Configuration
**Referenced in SecurityFilterChainConfig**

57. **CORS Configuration Not Reviewed**
    - **Issue**: CORS settings not visible in reviewed files
    - **Problem**: May be too permissive or too restrictive
    - **Severity**: MEDIUM
    - **Action**: Review CorsConfig.java

---

## Security Issues

### General Security Concerns

58. **No Input Sanitization**
    - **Issue**: User inputs not sanitized before use
    - **Problem**: XSS, SQL injection risks
    - **Severity**: HIGH
    - **Files**: Multiple controllers

59. **Passwords Potentially Logged**
    - **Issue**: Request logging might log passwords
    - **Problem**: Passwords exposed in logs
    - **Severity**: CRITICAL
    - **Action**: Review logging configuration

60. **No API Request Signing**
    - **Issue**: API requests not signed
    - **Problem**: Man-in-the-middle attacks possible
    - **Severity**: MEDIUM
    - **Action**: Consider request signing for sensitive operations

61. **No Database Encryption**
    - **Issue**: No field-level encryption mentioned in code
    - **Problem**: Sensitive data like emails stored in plaintext
    - **Severity**: HIGH
    - **Action**: Review database security

62. **Session Fixation Possible**
    - **Issue**: No session fixation protection explicitly configured
    - **Problem**: Session hijacking attacks possible
    - **Severity**: MEDIUM
    - **Action**: Review Spring Security session management

63. **No Content Security Policy**
    - **Issue**: No CSP headers configured
    - **Problem**: XSS attacks easier to execute
    - **Severity**: MEDIUM
    - **Action**: Add CSP headers

64. **Playwright Security**
    - **Issue**: Playwright with --no-sandbox flag
    - **Problem**: Running browser without sandbox is security risk
    - **Severity**: HIGH
    - **File**: Scraper.java, line 157

---

## Performance Issues

65. **No Database Connection Pooling Configuration**
    - **Issue**: Connection pool settings not visible
    - **Problem**: May run out of connections under load
    - **Severity**: MEDIUM
    - **Action**: Review application.yml

66. **No Query Optimization**
    - **Issue**: Repositories use simple findAll() calls
    - **Problem**: Performance issues with large datasets
    - **Severity**: MEDIUM
    - **Action**: Review repository methods

67. **No Caching Strategy**
    - **Issue**: No caching annotations or configuration visible
    - **Problem**: Repeated database queries for same data
    - **Severity**: MEDIUM
    - **Action**: Consider adding Spring Cache

68. **Synchronous Email Sending**
    - **Issue**: Some email methods block
    - **Problem**: Slows down request processing
    - **Severity**: LOW
    - **File**: EmailServiceImpl.java

---

## Code Quality Issues

69. **Missing JavaDoc**
    - **Issue**: Many methods lack documentation
    - **Problem**: Difficult for new developers to understand
    - **Severity**: LOW
    - **Files**: Multiple

70. **Magic Numbers**
    - **Issue**: Hardcoded numbers throughout code
    - **Problem**: Difficult to maintain, unclear meaning
    - **Severity**: LOW
    - **Examples**: Thread.sleep(100), batch sizes, timeouts

71. **Deep Nesting**
    - **Issue**: Some methods have deep nesting levels
    - **Problem**: Hard to read and maintain
    - **Severity**: LOW
    - **Files**: ApiCaller.java, Scraper.java

72. **Long Methods**
    - **Issue**: Some methods exceed 100 lines
    - **Problem**: Difficult to test and maintain
    - **Severity**: LOW
    - **Files**: Scraper.java, ApiCaller.java

73. **Duplicate Code**
    - **Issue**: Similar error handling in multiple places
    - **Problem**: Maintenance burden, inconsistency risk
    - **Severity**: LOW
    - **Action**: Extract common error handling

---

## Testing Issues

74. **No Integration Tests Visible**
    - **Issue**: Only unit tests found in review
    - **Problem**: Integration issues not caught
    - **Severity**: MEDIUM
    - **Action**: Review test coverage

75. **No Load/Performance Tests**
    - **Issue**: No performance testing infrastructure visible
    - **Problem**: Performance issues not discovered until production
    - **Severity**: MEDIUM
    - **Action**: Add performance tests

---

## Dependency Issues

76. **Playwright Version**
    - **Issue**: Using Playwright 1.52.0
    - **Problem**: Should regularly update for security patches
    - **Severity**: LOW
    - **File**: pom.xml

77. **Multiple Logging Frameworks**
    - **Issue**: Both Log4j2 and Slf4j annotations used
    - **Problem**: Inconsistent logging, potential conflicts
    - **Severity**: LOW
    - **Files**: Multiple

78. **JVPpeteer Dependency**
    - **Issue**: jvppeteer included but usage not visible
    - **Problem**: Unused dependency increasing build size
    - **Severity**: LOW
    - **File**: pom.xml

---

## Monitoring and Observability Issues

79. **No Distributed Tracing**
    - **Issue**: No tracing framework (e.g., Sleuth, Zipkin)
    - **Problem**: Difficult to debug issues in production
    - **Severity**: MEDIUM
    - **Action**: Consider adding distributed tracing

80. **Limited Metrics**
    - **Issue**: Only Prometheus metrics via Actuator
    - **Problem**: May need more detailed business metrics
    - **Severity**: LOW
    - **Action**: Review metrics strategy

81. **No Alerting Configuration**
    - **Issue**: No alerting on errors or anomalies
    - **Problem**: Issues not detected proactively
    - **Severity**: MEDIUM
    - **Action**: Configure alerting

---

## Database Issues

82. **Flyway Migration Files**
    - **Issue**: Need to review migration scripts for issues
    - **Problem**: Poorly written migrations can cause data loss
    - **Severity**: HIGH
    - **Action**: Review V1 and V2 migration files

83. **No Database Backup Strategy Visible**
    - **Issue**: Backup strategy not in code
    - **Problem**: Data loss risk
    - **Severity**: HIGH
    - **Action**: Ensure backups are configured

84. **Soft Delete Implementation**
    - **Issue**: User.deleted field exists but handling not reviewed
    - **Problem**: Soft-deleted data may leak or cause issues
    - **Severity**: MEDIUM
    - **Action**: Review soft delete implementation

---

## Priority Summary

### CRITICAL (Fix Immediately)
- **Issue #59**: Passwords potentially logged - Request logging might expose passwords in application logs
- **Issue #17**: Refresh token storage security - Need to verify refresh tokens are hashed, not stored in plaintext

### HIGH Priority
- Issue #6: No role-based access control on AI endpoints
- Issue #8: In-memory rate limiting won't persist
- Issue #11: Memory leak in rate limiting service
- Issue #12: Rate limit data not persisted
- Issue #14: Weak secret key handling
- Issue #19: Async email errors swallowed
- Issue #23: Path traversal vulnerability in attachments
- Issue #29: No concurrent scraping
- Issue #42: V2Ray process management issues
- Issue #51: No exception logging
- Issue #55: Actuator endpoints publicly accessible
- Issue #56: No gateway-level rate limiting
- Issue #58: No input sanitization
- Issue #61: No database encryption
- Issue #64: Playwright running without sandbox
- Issue #82: Review Flyway migrations
- Issue #83: No database backup strategy

### MEDIUM Priority
- **Issue #2**: No rate limiting for Gemini API calls
- **Issue #3**: No timeout configuration for AI API calls
- **Issue #5**: No input length validation on AI prompts
- **Issue #7**: Missing proper DTOs for AI controller
- **Issue #9**: Password policy not enforced
- **Issue #10**: No permanent account lockout on repeated verification failures
- **Issue #15**: No JWT key rotation mechanism
- **Issue #18**: No email queue/retry mechanism for failed deliveries
- **Issue #20**: Thread.sleep() used for rate limiting in mass email
- **Issue #22**: Mass email processing not resilient to crashes
- **Issue #24**: Playwright instance created per job instead of reused
- **Issue #26**: Proxies used without health checking
- **Issue #31**: File read failures silently mark data as parsed
- **Issue #34**: No caching of AI-generated search queries
- **Issue #36**: No duplicate detection for generated queries
- **Issue #37**: Random API key selection without health tracking
- **Issue #38**: No API key health monitoring
- **Issue #39**: Blocking calls on reactive WebClient
- **Issue #41**: No circuit breaker for failing API calls
- **Issue #43**: No limits on V2Ray process output logging
- **Issue #44**: Port allocation race condition in V2Ray service
- **Issue #47**: No health monitoring for V2Ray processes
- **Issue #49**: Pipelines can run indefinitely without timeout
- **Issue #50**: Generic exception handler too broad
- **Issue #52**: Exception details exposed to clients
- **Issue #57**: CORS configuration needs review
- **Issue #60**: No API request signing for sensitive operations
- **Issue #62**: No session fixation protection configured
- **Issue #63**: No Content Security Policy headers
- **Issue #65**: Database connection pooling not configured
- **Issue #66**: No query optimization for large datasets
- **Issue #67**: No caching strategy implemented
- **Issue #74**: Integration test coverage needs review
- **Issue #75**: No load/performance tests
- **Issue #79**: No distributed tracing
- **Issue #81**: No alerting configuration
- **Issue #84**: Soft delete implementation needs review

### LOW Priority
- All remaining issues

---

## Recommendations

1. **Immediate Actions**:
   - Review and fix all CRITICAL and HIGH priority issues
   - Implement proper logging and monitoring
   - Add security headers and protections
   - Fix rate limiting persistence

2. **Short-term Actions**:
   - Implement caching strategy
   - Add concurrent scraping
   - Fix async error handling
   - Review and secure all endpoints

3. **Long-term Actions**:
   - Add comprehensive integration tests
   - Implement distributed tracing
   - Refactor long methods and duplicated code
   - Add API request signing
   - Implement circuit breakers

4. **Architectural Improvements**:
   - Consider Redis for rate limiting
   - Add message queue for email sending
   - Implement proper job queue for scraping
   - Add service mesh for microservices patterns

---

*Document Generated*: 2025-12-08
*Total Issues Found*: 84
*Files Reviewed*: 127 Java files across 7 modules
