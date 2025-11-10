# Stage 1: Build
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /workspace

# Copy Maven wrapper and pom files
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY packages/java-common/pom.xml packages/java-common/
COPY apps/owner-backend-spring/pom.xml apps/owner-backend-spring/

# Download dependencies
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY packages/java-common/src packages/java-common/src
COPY apps/owner-backend-spring/src apps/owner-backend-spring/src
COPY apps/owner-backend-spring/lombok.config apps/owner-backend-spring/

# Build application
RUN ./mvnw clean package -pl apps/owner-backend-spring -am -DskipTests

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
