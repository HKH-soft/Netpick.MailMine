# Backend Architecture Analysis & Test Suite

## Architecture Overview

### Modular Structure
The Netpick backend follows a modular monolith architecture with clear separation of concerns:

- **gatekeeper** - Authentication, Authorization, Security (MFA, IP Policies, Rate Limiting)
- **taskfarm** - Task/Project Management
- **filefarm** - File Storage and Management
- **dealfarm** - Deal Management
- **core** - Shared utilities, base classes, common result types

### Key Technologies
- Spring Boot 3.5.6
- Java 21
- PostgreSQL (production) / H2 (test)
- Redis (caching, rate limiting)
- JWT for authentication
- Flyway for migrations
- Lombok for boilerplate reduction
- JUnit 5 + Mockito for testing

---

## Data Models

### Core Entity Pattern
All entities extend `BaseEntity` with:
- `id` (UUID, generated)
- `description` (optional)
- `createdAt`, `updatedAt` (audit timestamps)
- `deleted` (soft delete flag)

### User Model
- Email (unique, required)
- Password hash (encoded)
- Name, profile image
- Role relationship (USER, ADMIN, SUPER_ADMIN)
- MFA enabled flag
- Verification embedded object

### Task Model
- Title, description
- Status (TODO, IN_PROGRESS, IN_REVIEW, BLOCKED, DONE)
- Priority (LOW, MEDIUM, HIGH, URGENT)
- Project/Assignee/Creator relationships
- Due date, order index, completion timestamp

### Project Model
- Name, description
- Status (ACTIVE, ARCHIVED, COMPLETED)
- Owner relationship

---

## API Endpoints

### Authentication (`/api/v1/gatekeeper/auth`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /sign-up | Register new user |
| POST | /sign-in | Authenticate user (returns JWT) |
| POST | /verify | Verify email with code |
| POST | /resend-verification | Resend verification code |
| POST | /refresh | Refresh access token |
| POST | /logout | Logout from current device |
| POST | /logout-all | Logout from all devices |
| POST | /password-reset/request | Request password reset |
| POST | /password-reset/verify | Verify reset code |
| POST | /password-reset/confirm | Confirm new password |

### TaskFarm (`/api/v1/taskfarm`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /tasks | List all tasks (paginated) |
| GET | /tasks/{id} | Get task by ID |
| POST | /tasks | Create task |
| PUT | /tasks/{id} | Update task |
| DELETE | /tasks/{id} | Soft delete task |
| PUT | /tasks/{id}/restore | Restore deleted task |
| GET | /tasks/status/{status} | Filter by status |
| GET | /tasks/project/{projectId} | Filter by project |
| PUT | /tasks/{id}/reorder | Reorder task in column |

### FileFarm (`/api/v1/filefarm`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /files | List all files |
| GET | /files/folder/{folderId} | Files by folder |
| GET | /files/search | Search by name |
| POST | /files | Upload file |
| PUT | /files/{id} | Update metadata |
| DELETE | /files/{id} | Soft delete |

---

## Business Logic

### Password Validation (UserService)
- Minimum 12 characters
- Requires 3 of 4 character types (lower, upper, digit, special)
- Cannot contain email local-part or name
- Cannot be in common passwords list
- zxcvbn entropy check (minimum score 3)

### Rate Limiting
- **Login**: 5 attempts, 15-minute lockout
- **Verification**: Configurable max attempts, 10-minute cooldown
- **Resend**: 3 per hour, 30-second minimum interval

### MFA Implementation
- TOTP (Time-based One-Time Password) with HMAC-SHA1
- Backup codes for recovery
- QR code generation for setup
- Timing-attack resistant comparison

### Security Pipeline
1. IP Policy check
2. Rate limiting validation
3. Credential authentication
4. Anomaly detection (risk scoring)
5. MFA verification (if enabled)
6. Device session tracking
7. Security event logging

---

## Integration Points

### External Services
- Email (SMTP via Spring Boot Mail)
- Redis (caching, distributed rate limiting)
- PostgreSQL (primary database)
- Telegram Bot API (notifications)
- SMS.ir (SMS gateway)

### Internal Services
- UserService <-> UserRepository (JPA)
- TaskService <-> TaskRepository (JPA)
- FileService <-> FileRepository (JPA)
- AuthenticationService orchestrates multiple services

---

## Test Suite Summary

### Unit Tests Created (20 test classes, ~120+ tests)
| File | Tests | Coverage |
|------|-------|----------|
| TaskServiceTest.java | 17 | CRUD, filtering, reordering |
| ProjectServiceTest.java | 11 | Project CRUD, stats |
| UserServiceTest.java | 20+ | Password validation, user management |
| RateLimitingServiceTest.java | 12 | Rate limit logic |
| MfaServiceTest.java | 12 | MFA setup, enable, disable |
| AuthenticationServiceUnitTest.java | 6 | Auth flow security |
| FileServiceTest.java | 12 | Upload, validation, CRUD |
| LabelServiceTest.java | 10 | Label CRUD, task association |
| CommentServiceTest.java | 12 | Comment CRUD, task filtering |
| RefreshTokenServiceTest.java | 6 | Token lifecycle, rotation |
| VerificationServiceTest.java | 4 | Code verification |
| AnomalyDetectionServiceTest.java | 2 | Risk scoring |
| IpPolicyServiceTest.java | 6 | IP policy CRUD, blocking |
| PageDTOMapperTest.java | 4 | Pagination mapping |
| TaskTest.java | 4 | Entity behavior |
| UserTest.java | 5 | User entity, roles |
| ResultTest.java | 12 | Result monad operations |

### Integration Tests Created (3 test classes, 17 tests)
| File | Scope |
|------|-------|
| TaskControllerIntegrationTest.java | All REST endpoints |
| AuthenticationControllerIntegrationTest.java | Auth endpoints |
| TaskServiceIntegrationTest.java | Full CRUD with H2 |
| AuthenticationE2ETest.java | Registration/auth flow |

### Test Configuration
- `application-test.yml` for H2 in-memory database
- Test profile disables Flyway, uses Hibernate DDL
- All unit tests use Mockito for isolation

### Running Tests
```bash
./mvnw test
# Or with the Maven wrapper:
./mvnw test-compile exec:java -Dexec.mainClass="org.junit.platform.console.ConsoleLauncher"
```