#######################################################################
# 1 ─ BUILD STAGE  (Maven 3.9 + Temurin 21, Ubuntu 22.04 "jammy")     #
#######################################################################
# LTS used to run the project: Eclipse Temurin Java 21 (Temurin support until at least Dec 2029).
FROM maven:3.9.8-eclipse-temurin-21-jammy AS build
WORKDIR /workspace

# ---- dependency caching ----------------------------------------
COPY pom.xml mvnw ./
COPY .mvn/ .mvn/
RUN chmod +x mvnw && ./mvnw -B dependency:go-offline --no-transfer-progress

# ---- copying source code and packaging -------------------------------
COPY src/ src/
RUN ./mvnw -B clean package -DskipTests --no-transfer-progress


#######################################################################
# 2 ─ RUNTIME STAGE  (Distroless Java 21, Debian 12 "bookworm")       #
#######################################################################
# LTS used to run the project: Eclipse Temurin Java 21 Distroless (Debian 12).
FROM gcr.io/distroless/java21-debian12@sha256:c298bfc8c8b1aa3d7b03480dcf52001a90d66d966f6a8d8997ae837d3982be3f AS runtime
WORKDIR /app

COPY --from=build /workspace/target/*.jar app.jar


# --------------------------------------------------------------------
# JVM tuning for 1 vCPU / 1 GiB                                      #
# --------------------------------------------------------------------
ENV JAVA_TOOL_OPTIONS="\
 -XX:+UseG1GC \
 -XX:MaxGCPauseMillis=200 \
 -XX:ParallelGCThreads=1 \
 -XX:ConcGCThreads=1 \
 -XX:MaxRAMPercentage=50 \
 -XX:InitialRAMPercentage=25 \
 -XX:MaxMetaspaceSize=256m \
 -XX:ReservedCodeCacheSize=96m \
 -XX:+UseStringDeduplication \
 -Djdk.virtualThreadScheduler.parallelism=1 \
 -XX:+ExitOnOutOfMemoryError \
"


# --------------------------------------------------------------------
# Default environment variables (dev/local)                          #
# -> Override using 'docker run -e ...' for production/staging       #
# --------------------------------------------------------------------
ENV SERVER_PORT=8080

EXPOSE 8080

# Distroless: ENTRYPOINT as an array
ENTRYPOINT [ "java", "-jar", "/app/app.jar" ]
