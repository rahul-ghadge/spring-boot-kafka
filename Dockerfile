# Single-stage build — avoids Windows Docker volume/cache issues
FROM maven:3.9.7-eclipse-temurin-21 AS builder

WORKDIR /workspace

# Copy pom first → cache deps layer separately from source
COPY pom.xml .
RUN mvn dependency:go-offline -B --no-transfer-progress

# Copy source and build
COPY src src
RUN mvn clean package -DskipTests -B --no-transfer-progress

# ─── Runtime ────────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-jammy

RUN groupadd --system appgroup && useradd --system --gid appgroup appuser
WORKDIR /app

COPY --from=builder /workspace/target/*.jar app.jar
RUN chown appuser:appgroup app.jar
USER appuser

EXPOSE 8080

ENTRYPOINT ["java", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-jar", "app.jar"]
