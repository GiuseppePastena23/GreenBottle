# syntax=docker/dockerfile:1

# ---- Build stage ----
FROM eclipse-temurin:21-jdk AS build

WORKDIR /build

# Tools used by the Maven Wrapper (mvnw) to download and unpack Maven.
RUN apt-get update && apt-get install -y --no-install-recommends curl unzip ca-certificates \
    && rm -rf /var/lib/apt/lists/*

# The wrapper is committed without the executable bit, so chmod it explicitly.
COPY mvnw .
COPY .mvn .mvn

# Cache dependencies separately from sources.
COPY pom.xml .
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline

# Build the executable Spring Boot jar (tests run in CI, not in the image build).
COPY src ./src
RUN ./mvnw -B package -DskipTests

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre

WORKDIR /app

# Run as an unprivileged user.
RUN groupadd --system app && useradd --system --gid app --home-dir /app app

COPY --from=build /build/target/greenbottle-0.0.1-SNAPSHOT.jar /app/app.jar

# RenderCliente reads this file from a path relative to the working directory.
COPY --from=build /build/src/main/resources/static/img/defaultImgCliente.png \
     /app/src/main/resources/static/img/defaultImgCliente.png

RUN chown -R app:app /app
USER app

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
