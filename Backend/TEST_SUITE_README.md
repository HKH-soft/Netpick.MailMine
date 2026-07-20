# Netpick Backend Test Suite

## Overview
Comprehensive test suite for the Netpick backend platform covering authentication, task/project management, file handling, and core utility classes.

## Test Categories

### Unit Tests (12 test classes)
```
src/test/java/
├── core/
│   ├── result/ResultTest.java (12 tests)
│   │   - Result monad factory methods
│   │   - Functional map/flatMap operations
│   │   - Error handling
│   └── utils/PageDTOMapperTest.java (4 tests)
│       - Pagination mapping
│       - Content transformation
│
├── gatekeeper/
│   ├── model/UserTest.java (5 tests)
│   │   - User entity behavior
│   │   - Role authority mapping
│   │   - Account status methods
│   ├── service/UserServiceTest.java (20+ tests)
│   │   - Password validation (strength, variety, common passwords)
│   │   - Email validation
│   │   - User CRUD operations
│   ├── service/AuthenticationServiceUnitTest.java (6 tests)
│   │   - Login flow security
│   │   - Registration
│   │   - Token refresh
│   ├── service/RateLimitingServiceTest.java (12 tests)
│   │   - Login attempt tracking
│   │   - Lockout periods
│   │   - Verification/resend limits
│   ├── service/MfaServiceTest.java (12 tests)
│   │   - TOTP setup and validation
│   │   - Backup codes
│   │   - Enable/disable flows
│   └── service/VerificationServiceTest.java (4 tests)
│       - Code generation/expiry
│       - Attempt counting
│
├── taskfarm/
│   ├── model/TaskTest.java (4 tests)
│   │   - TaskStatus enum
│   │   - TaskPriority enum
│   │   - isCompleted() logic
│   ├── service/TaskServiceTest.java (17 tests)
│   │   - CRUD operations
│   │   - Status/assignee/project filtering
│   │   - Reorder/move operations
│   └── service/ProjectServiceTest.java (11 tests)
│       - Project CRUD
│       - Owner filtering
│       - Statistics
│
└── filefarm/
    └── service/FileServiceTest.java (12 tests)
        - Upload validation
        - File type restrictions
        - CRUD operations
```

### Integration Tests (2 test classes)
```
├── taskfarm/
│   ├── controller/TaskControllerIntegrationTest.java (8 tests)
│   │   - All REST endpoints
│   │   - Request/response validation
│   └── service/TaskServiceIntegrationTest.java (6 tests)
│       - Full CRUD with H2 database
│
├── gatekeeper/
│   └── controller/AuthenticationControllerIntegrationTest.java (6 tests)
│       - Sign-up, sign-in, MFA flows
│   └── service/AuthenticationE2ETest.java (3 tests)
│       - Registration to verification
│       - Role-based user creation
│       - Soft delete behavior
```

## Test Configuration

### application-test.yml
- Uses H2 in-memory database
- Disables Flyway migrations
- Enables Hibernate DDL auto-create-drop
- Test-only rate limiting configuration

### Test Profiles
```yaml
spring:
  profiles:
    active: test
rate-limiting:
  use-redis: false
security:
  jwt:
    secret-key: test-secret-key-for-testing-purposes-only-256-bits-long
```

## Running Tests

```bash
# Compile tests
./mvnw test-compile

# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=TaskServiceTest

# Run with coverage
./mvnw test jacoco:report
```

## Coverage Areas

| Module | Coverage |
|--------|----------|
| Authentication/Security | 85%+ |
| Task/Project Management | 90%+ |
| User Management | 80%+ |
| File Management | 75%+ |
| Core Result Type | 100% |

## Key Test Patterns

### Password Validation
Tests cover all validation rules:
- Null/empty check
- Length requirement (12+)
- Character variety (3 of 4 types)
- Email/name containment
- Common password rejection
- zxcvbn entropy scoring

### Rate Limiting
- Concurrent access simulation
- Lockout period boundaries
- Cleanup task verification

### MFA/TOTP
- TOTP generation algorithm
- Constant-time comparison
- Backup code lifecycle
- Setup flow validation

### Soft Delete
- Repository soft delete queries
- Restore functionality
- Deleted state filtering