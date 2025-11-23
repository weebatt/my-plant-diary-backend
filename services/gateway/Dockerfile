# syntax=docker/dockerfile:1
FROM gradle:8.7-jdk21 AS build
WORKDIR /home/gradle/src
COPY . .
RUN gradle :services:gateway:bootJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /home/gradle/src/services/gateway/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]

