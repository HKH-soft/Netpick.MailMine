# MailMine Backend

A powerful Spring Boot backend service for web scraping and lead generation, providing RESTful APIs for the MailMine platform.

## 🚀 Overview

The MailMine backend is built with Spring Boot 3.5.6 and Java 21, leveraging modern technologies for web scraping, authentication, and data management. It provides a robust API layer for the frontend to interact with scraping services, user management, and data processing.

## 🛠️ Tech Stack

- **Framework**: Spring Boot 3.5.6
- **Language**: Java 21
- **Database**: PostgreSQL 15+ with Flyway migrations
- **Caching**: Redis (optional, with fallback to in-memory)
- **Security**: Spring Security with JWT authentication
- **Resilience**: Spring Retry with exponential backoff
- **Web Scraping**: 
  - Playwright (browser automation)
  - Jsoup (HTML parsing)
  - jvppeteer (headless browser control)
- **AI Integration**: Google Gemini AI
- **API Documentation**: SpringDoc OpenAPI (Swagger UI)
- **Monitoring**: Spring Actuator with Prometheus metrics
- **Build Tool**: Maven 3.x

## 📁 Project Structure

```
Backend/
├── src/
│   ├── main/
│   │   ├── java/ir/netpick/mailmine/
│   │   │   ├── auth/              # Authentication & Authorization
│   │   │   │   ├── controller/    # REST endpoints for auth
│   │   │   │   ├── service/       # Business logic for auth
│   │   │   │   ├── jwt/           # JWT token handling
│   │   │   │   ├── model/         # User entities
│   │   │   │   ├── dto/           # Data transfer objects
│   │   │   │   └── security/      # Security configuration
│   │   │   ├── scrape/            # Web Scraping Module
│   │   │   │   ├── controller/    # REST endpoints for scraping
│   │   │   │   ├── service/       # Scraping business logic
│   │   │   │   ├── parser/        # HTML/URL parsing utilities
│   │   │   │   ├── model/         # Scrape job entities
│   │   │   │   ├── dto/           # DTOs for scrape operations
│   │   │   │   └── repository/    # Data access layer
│   │   │   ├── email/             # Email Management
│   │   │   │   ├── controller/    # Email endpoints
│   │   │   │   ├── service/       # Email service logic
│   │   │   │   └── dto/           # Email DTOs
│   │   │   ├── ai/                # AI Integration
│   │   │   │   ├── controller/    # AI endpoints
│   │   │   │   └── service/       # AI service (Gemini)
│   │   │   └── init/              # Application initialization
│   │   └── resources/
│   │       ├── application.yml           # Main configuration
│   │       ├── application-dev.yml       # Development profile
│   │       ├── application-pro.yml       # Production profile
│   │       ├── db/migration/             # Flyway database migrations
│   │       ├── templates/                # Thymeleaf email templates
│   │       └── logback.xml              # Logging configuration
│   └── test/
│       └── java/ir/netpick/mailmine/    # Unit and integration tests
├── pom.xml                              # Maven dependencies
├── mvnw                                 # Maven wrapper (Unix)
└── mvnw.cmd                             # Maven wrapper (Windows)
```

## 🏁 Getting Started

### Prerequisites

- Java Development Kit (JDK) 21 or higher
- PostgreSQL 15+ database server
- Redis 7+ (optional, for distributed caching and rate limiting)
- Maven 3.x (or use the included Maven wrapper)
- Playwright browser binaries (installed automatically on first run)

### Installation

1. **Clone the repository** (if not already done)
   ```bash
   git clone https://github.com/HKH-soft/Netpick.MailMine.git
   cd Netpick.MailMine/Backend
   ```

2. **Configure Database**
   
   Create a PostgreSQL database:
   ```sql
   CREATE DATABASE mailmine;
   ```

   Update `src/main/resources/application.yml` with your database credentials:
   ```yaml
   spring:
     datasource:
       url: jdbc:postgresql://localhost:5432/mailmine
       username: your_username
       password: your_password
   ```

3. **Configure Application Settings**
   
   Review and update other settings in `application.yml`:
   - JWT secret key (for production, use a strong secret)
   - Email server settings (if using email features)
   - AI service configuration (Google Gemini API key)
   - Proxy settings (if required)
   - Redis settings (optional, see [Redis Integration Guide](REDIS_INTEGRATION.md))

4. **Optional: Setup Redis** (for distributed caching and rate limiting)
   
   See the comprehensive [Redis Integration Guide](REDIS_INTEGRATION.md) for:
   - Redis installation and configuration
   - Caching setup and benefits
   - Distributed rate limiting
   - Performance tuning
   
   **Quick Start with Docker**:
   ```bash
   docker run -d -p 6379:6379 --name redis redis:7-alpine
   ```
   
   Update environment variables:
   ```bash
   export REDIS_HOST=localhost
   export REDIS_PORT=6379
   ```

5. **Build the Project**
   ```bash
   # Using Maven wrapper (recommended)
   ./mvnw clean install
   
   # Or using system Maven
   mvn clean install
   ```

6. **Run Database Migrations**
   
   Flyway migrations run automatically on application startup. To run manually:
   ```bash
   ./mvnw flyway:migrate
   ```

7. **Run the Application**
   ```bash
   # Development mode
   ./mvnw spring-boot:run
   
   # With specific profile
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
   ```

   The backend API will be available at `http://localhost:8080`

## 🧪 Testing

### Run Unit Tests
```bash
./mvnw test
```

### Run Integration Tests
```bash
./mvnw verify
```

### Run Specific Test Class
```bash
./mvnw test -Dtest=AuthServiceTest
```

## 📚 API Documentation

Once the application is running, access the interactive API documentation:

- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`

### Key API Endpoints

#### Authentication
- `POST /api/auth/register` - Register a new user
- `POST /api/auth/login` - Login and receive JWT token
- `POST /api/auth/refresh` - Refresh JWT token
- `POST /api/auth/logout` - Logout user

#### Scraping
- `POST /api/scrape/start` - Start a new scraping job
- `GET /api/scrape/jobs` - List all scraping jobs
- `GET /api/scrape/jobs/{id}` - Get job details
- `DELETE /api/scrape/jobs/{id}` - Cancel/delete a job
- `GET /api/scrape/results` - Get scraping results

#### Email Management
- `GET /api/emails` - List collected emails
- `POST /api/emails/export` - Export emails to CSV
- `DELETE /api/emails/{id}` - Delete an email record

#### AI Services
- `POST /api/ai/generate-query` - Generate search queries using AI
- `POST /api/ai/analyze` - Analyze scraped content

## 📊 Monitoring & Metrics

The application exposes metrics via Spring Actuator:

- **Health Check**: `http://localhost:8080/actuator/health`
- **Prometheus Metrics**: `http://localhost:8080/actuator/prometheus`
- **Application Info**: `http://localhost:8080/actuator/info`

To enable all actuator endpoints, update `application.yml`:
```yaml
management:
  endpoints:
    web:
      exposure:
        include: "*"
```

## 🔒 Security

### Authentication
The backend uses JWT (JSON Web Tokens) for stateless authentication:
- Tokens are issued on successful login
- Tokens must be included in the `Authorization` header: `Bearer <token>`
- Token expiration is configurable in `application.yml`

### Authorization
Role-based access control (RBAC) with roles:
- `ROLE_USER` - Standard user access
- `ROLE_ADMIN` - Administrative access

### Security Best Practices
- Passwords are hashed using BCrypt
- JWT secrets should be strong and environment-specific
- HTTPS should be enabled in production
- CORS is configured for the frontend origin

## 🐳 Docker Support

Build Docker image using Jib:
```bash
# Set Docker credentials
export DOCKER_USERNAME=your_docker_username

# Build and push image
./mvnw jib:build -Ddocker.image.tag=1.0.0
```

Or use Docker Compose (if available):
```bash
docker-compose up -d
```

## ⚙️ Configuration

### Profiles

The application supports multiple Spring profiles:

- **dev** - Development environment (`application-dev.yml`)
- **pro** - Production environment (`application-pro.yml`)
- **default** - Default configuration (`application.yml`)

Activate a profile:
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Or via environment variable:
```bash
export SPRING_PROFILES_ACTIVE=dev
./mvnw spring-boot:run
```

### Key Configuration Properties

```yaml
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/mailmine
spring.datasource.username=postgres
spring.datasource.password=password

# Redis (optional)
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.data.redis.password=  # Leave empty if no password
spring.cache.type=redis  # Set to 'none' to disable caching

# Rate Limiting
rate-limiting.use-redis=false  # Set to true for distributed rate limiting

# JWT
jwt.secret=your-secret-key-change-in-production
jwt.expiration=86400000  # 24 hours in milliseconds

# Scraping
scraping.max-concurrent-jobs=5
scraping.timeout-seconds=300
scraping.user-agent=MailMine/1.0

# AI (Google Gemini)
google.ai.api-key=your-gemini-api-key
google.ai.model=gemini-pro
```

## 🔧 Development

### Code Style
The project follows standard Java conventions:
- Use 4 spaces for indentation
- Follow Spring Framework best practices
- Write unit tests for service layer
- Write integration tests for controllers

### Lombok
The project uses Lombok to reduce boilerplate code. Make sure your IDE has Lombok plugin installed:
- IntelliJ IDEA: Enable annotation processing
- Eclipse: Install Lombok
- VS Code: Install Lombok extension

### Building Without Tests
```bash
./mvnw clean install -DskipTests
```

### Hot Reload
Spring Boot DevTools is included for development hot reload:
```bash
./mvnw spring-boot:run
```
Changes to Java files will trigger automatic restart.

## 🐛 Troubleshooting

### Database Connection Issues
- Verify PostgreSQL is running: `systemctl status postgresql`
- Check database credentials in `application.yml`
- Ensure database exists: `psql -l`

### Port Already in Use
If port 8080 is occupied:
```bash
# Change port in application.yml
server:
  port: 8081
```

### Playwright Issues
First run downloads browser binaries. If issues occur:
```bash
# Install browsers manually
npx playwright install
```

### Maven Wrapper Permission
```bash
chmod +x mvnw
```

### Redis Connection Issues
If you're using Redis and encounter connection problems:
```bash
# Check if Redis is running
redis-cli ping  # Should return PONG

# Check Redis logs
docker logs redis  # If using Docker

# Disable Redis temporarily
# Set in application.yml:
spring.cache.type: none
rate-limiting.use-redis: false
```

For detailed Redis troubleshooting, see [Redis Integration Guide](REDIS_INTEGRATION.md#troubleshooting).

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 📧 Contact

For backend-specific issues, please create an issue on GitHub:
[https://github.com/HKH-soft/Netpick.MailMine/issues](https://github.com/HKH-soft/Netpick.MailMine/issues)

## 🔗 Related Documentation

- [Redis Integration Guide](REDIS_INTEGRATION.md) - Caching, rate limiting, and retry setup
- [Main Project README](../README.md)
- [Frontend README](../Frontend/README.md)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Security Documentation](https://spring.io/projects/spring-security)
- [Playwright Java Documentation](https://playwright.dev/java/)
