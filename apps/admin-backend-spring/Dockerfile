# Stage 1: Build
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /workspace

# Copy Maven wrapper and pom files
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY packages/java-common/pom.xml packages/java-common/
COPY apps/admin-backend-spring/pom.xml apps/admin-backend-spring/

# Download dependencies
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY packages/java-common/src packages/java-common/src
COPY apps/admin-backend-spring/src apps/admin-backend-spring/src
COPY apps/admin-backend-spring/lombok.config apps/admin-backend-spring/

# Build application
RUN ./mvnw clean package -pl apps/admin-backend-spring -am -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create non-root user
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# Copy JAR from builder
COPY --from=builder /workspace/apps/admin-backend-spring/target/*.jar app.jar

# Change ownership
RUN chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
