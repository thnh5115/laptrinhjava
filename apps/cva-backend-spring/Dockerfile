# Stage 1: Build
FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /workspace

# Copy entire repo so Maven has all modules (shared libs + sibling services)
COPY . .

# Build only the CVA module (and its dependencies) as a fat jar
RUN mvn -pl apps/cva-backend-spring -am clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup -g 1001 -S appgroup \
    && adduser -u 1001 -S appuser -G appgroup

# Copy the Spring Boot fat jar (avoid original-*.jar)
COPY --from=builder /workspace/apps/cva-backend-spring/target/cva-backend-spring-*.jar /app/app.jar

RUN chown -R appuser:appgroup /app

USER appuser

EXPOSE 8083

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8083/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
