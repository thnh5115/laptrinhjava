# Stage 1: Build
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /workspace

# Copy Maven wrapper and pom files
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY packages/java-common/pom.xml packages/java-common/
COPY apps/admin-backend-spring/pom.xml apps/admin-backend-spring/
COPY apps/buyer-backend-spring/pom.xml apps/buyer-backend-spring/
COPY apps/cva-backend-spring/pom.xml apps/cva-backend-spring/
COPY apps/owner-backend-spring/pom.xml apps/owner-backend-spring/
COPY packages/java-common/src packages/java-common/src

# Include admin backend sources needed for compilation
COPY apps/admin-backend-spring/src apps/admin-backend-spring/src
COPY apps/admin-backend-spring/lombok.config apps/admin-backend-spring/

# Fix CRLF issues and ensure executable bit for mvnw
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw

# Build shared module
RUN ./mvnw -pl packages/java-common -am install -DskipTests

# Install admin backend (owner depends on its entities/repositories)
# Skip Spring Boot repackage so dependency jar exposes compiled classes
RUN ./mvnw -pl apps/admin-backend-spring -am install -DskipTests -Dspring-boot.repackage.skip=true

# Download dependencies for owner service (uses installed admin artifact)
RUN ./mvnw -pl apps/owner-backend-spring dependency:go-offline -B

# Copy source code
COPY apps/owner-backend-spring/src apps/owner-backend-spring/src
COPY apps/owner-backend-spring/lombok.config apps/owner-backend-spring/

# Build application (owner packaged as runnable Boot jar)
RUN ./mvnw clean package -pl apps/owner-backend-spring -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create non-root user
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# Copy JAR from builder
COPY --from=builder /workspace/apps/owner-backend-spring/target/*.jar app.jar

# Change ownership
RUN chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 8082

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8082/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
