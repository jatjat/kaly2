# Production built kaly2 with Gradle 4.5; the wrapper's 4.1 download no longer works
FROM gradle:4.5-jdk8-alpine AS build
USER root
WORKDIR /app
COPY build.gradle settings.gradle gradle.properties ./
COPY lib ./lib
COPY src ./src
# Exposed 0.8.6 was only published on Bintray, which shut down in 2021. Pass a
# Maven repo that still has it: docker build --build-context legacy-maven=<dir>
COPY --from=legacy-maven . /legacy-maven
ENV LEGACY_MAVEN_URL=file:///legacy-maven
RUN gradle --no-daemon build -x test

FROM eclipse-temurin:8-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/kaly2-0.1.jar ./
EXPOSE 9000
CMD ["java", "-XX:+UseConcMarkSweepGC", "-XX:CMSIncrementalSafetyFactor=50", "-XX:+UseParNewGC", \
     "-XX:+CMSConcurrentMTEnabled", "-XX:ConcGCThreads=2", "-XX:ParallelGCThreads=2", "-XX:GCTimeRatio=90", \
     "-XX:MaxGCPauseMillis=1", "-XX:GCPauseIntervalMillis=1000", "-jar", "kaly2-0.1.jar"]
