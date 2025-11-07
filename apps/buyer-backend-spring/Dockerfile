# =========================
# Stage 1: Build
# =========================
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /workspace
# copy toàn bộ monorepo để có parent + các module nội bộ
COPY ../../ /workspace/

# 1) Cài lib nội bộ java-common vào local repo của container
RUN mvn -B -DskipTests -pl packages/java-common clean install

# 2) Build buyer-backend-spring (kéo theo module phụ thuộc nếu cần)
RUN mvn -B -DskipTests -pl apps/buyer-backend-spring -am clean package

# =========================
# Stage 2: Runtime
# =========================
FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY --from=build /workspace/apps/buyer-backend-spring/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
