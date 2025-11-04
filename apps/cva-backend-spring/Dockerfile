# Stage 1: Build
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /workspace

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY packages/java-common/pom.xml packages/java-common/
COPY apps/admin-backend-spring/pom.xml apps/admin-backend-spring/
COPY apps/cva-backend-spring/pom.xml apps/cva-backend-spring/

RUN ./mvnw -pl apps/cva-backend-spring -am dependency:go-offline -B

COPY packages/java-common/src packages/java-common/src
COPY apps/cva-backend-spring/src apps/cva-backend-spring/src
COPY apps/cva-backend-spring/lombok.config apps/cva-backend-spring/

RUN ./mvnw clean package -pl apps/cva-backend-spring -am -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup -g 1001 -S appgroup \
    && adduser -u 1001 -S appuser -G appgroup

COPY --from=builder /workspace/apps/cva-backend-spring/target/*.jar app.jar

RUN chown -R appuser:appgroup /app

USER appuser

EXPOSE 8082

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8082/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
