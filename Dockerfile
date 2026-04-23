# ── Stage 1: build ──────────────────────────────────────────────────────────
# Uses the official Maven + JDK 21 image so no local toolchain is required.
# Works on Linux, Windows (Docker Desktop), and macOS without any changes.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# Copy dependency descriptors first to leverage Docker layer caching
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Copy source and build the fat JAR (skip tests — run them separately)
COPY src ./src
RUN mvn package -DskipTests -q

# ── Stage 2: runtime ─────────────────────────────────────────────────────────
# Minimal JRE image for the final artifact
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=build /build/target/football-confederation-1.0.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
