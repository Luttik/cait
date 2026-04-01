# JDK toolchain for Gradle / Kotlin Android work. Mount ANDROID_HOME for full SDK builds.
FROM eclipse-temurin:17-jdk-jammy

RUN apt-get update && apt-get install -y --no-install-recommends \
    git \
    unzip \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /workspace
