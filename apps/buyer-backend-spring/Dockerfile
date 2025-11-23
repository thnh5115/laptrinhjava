# Stage 1: Build
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /workspace

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY packages/java-common/pom.xml packages/java-common/
COPY apps/admin-backend-spring/pom.xml apps/admin-backend-spring/
COPY apps/buyer-backend-spring/pom.xml apps/buyer-backend-spring/
COPY apps/cva-backend-spring/pom.xml apps/cva-backend-spring/
COPY apps/owner-backend-spring/pom.xml apps/owner-backend-spring/

RUN sed -i 's/\r$//' mvnw && chmod +x mvnw

# Build shared modules needed for buyer
COPY packages/java-common/src packages/java-common/src
RUN ./mvnw -pl packages/java-common -am install -DskipTests

# Pre-download buyer dependencies
RUN ./mvnw -pl apps/buyer-backend-spring -am dependency:go-offline -B

# Copy buyer sources
COPY apps/buyer-backend-spring/src apps/buyer-backend-spring/src

# Build buyer backend
RUN ./mvnw clean package -pl apps/buyer-backend-spring -am -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

COPY --from=builder /workspace/apps/buyer-backend-spring/target/buyer-backend-spring-*.jar /app/app.jar

RUN chown -R appuser:appgroup /app

USER appuser

EXPOSE 8081

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8081/actuator/health || exit 1

ENTRYPOINT ["java","-jar","/app/app.jar"]
