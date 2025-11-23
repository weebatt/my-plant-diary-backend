# syntax=docker/dockerfile:1
FROM gradle:8.7-jdk21 AS build
WORKDIR /home/gradle/src
COPY . .
RUN gradle :services:outbox-publisher:bootJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /home/gradle/src/services/outbox-publisher/build/libs/*.jar app.jar
EXPOSE 8090
ENTRYPOINT ["java","-jar","/app/app.jar"]

