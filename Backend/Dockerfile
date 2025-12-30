# ===== 1. Build Stage =====
FROM maven:3.9.11-eclipse-temurin-21 AS builder
WORKDIR /app

# Cache dependencies
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Copy source
COPY src ./src

# Build JAR with Maven cache mount (persists downloads across builds)
RUN --mount=type=cache,target=/root/.m2 \
    mvn clean package -DskipTests

# ===== 2. Runtime Stage =====
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Install wget, curl, unzip for healthcheck and xray download
RUN apk add --no-cache wget curl unzip

# Download and install xray
RUN mkdir -p /app/xray && \
    cd /app/xray && \
    wget -q https://github.com/XTLS/Xray-core/releases/download/v25.12.8/Xray-linux-64.zip && \
    unzip -q Xray-linux-64.zip && \
    rm Xray-linux-64.zip && \
    chmod +x xray && \
    ln -s /app/xray/xray /usr/local/bin/xray

# Copy JAR from builder
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
