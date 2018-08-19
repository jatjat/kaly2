FROM openjdk:8-jdk-alpine AS build
WORKDIR /app/server
COPY gradle ./gradle
COPY gradlew .
COPY build.gradle .
COPY gradle.properties .
COPY lib ./lib
COPY src ./src
RUN ./gradlew check
RUN ./gradlew installDist


FROM openjdk:8-jre-alpine
WORKDIR /app/server
COPY --from=build /app/server/build/install/server .
EXPOSE 9000
ENTRYPOINT ["./bin/core"]
