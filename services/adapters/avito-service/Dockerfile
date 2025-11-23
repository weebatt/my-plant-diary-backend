# syntax=docker/dockerfile:1
FROM gradle:8.7-jdk21 AS build
WORKDIR /home/gradle/src
COPY . .
RUN gradle :services:adapters:avito-service:bootJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /home/gradle/src/services/adapters/avito-service/build/libs/*.jar app.jar
EXPOSE 8094
ENTRYPOINT ["java","-jar","/app/app.jar"]

