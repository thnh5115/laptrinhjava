# ---------- Build stage ----------
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /workspace

# Copy POMs trước để cache dependency
COPY pom.xml .
COPY packages/java-common/pom.xml packages/java-common/pom.xml
COPY apps/buyer-backend-spring/pom.xml apps/buyer-backend-spring/pom.xml

# Cài parent POM (nếu root là packaging=pom)
RUN mvn -B -q -DskipTests -N install

# Cài lib nội bộ java-common
RUN mvn -B -q -DskipTests -pl packages/java-common clean install

# Copy toàn bộ source
COPY . .

# Build buyer-backend-spring (kéo theo phụ thuộc cần thiết)
RUN mvn -B -q -DskipTests -pl apps/buyer-backend-spring -am package

# ---------- Runtime stage ----------
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /workspace/apps/buyer-backend-spring/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java","-jar","app.jar"]
