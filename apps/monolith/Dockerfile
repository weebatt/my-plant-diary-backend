# syntax=docker/dockerfile:1
FROM gradle:8.7-jdk21 AS build
WORKDIR /home/gradle/src
# Кэшируем зависимости Gradle и используем wrapper для стабильности
COPY gradlew ./gradlew
COPY gradle ./gradle
COPY settings.gradle.kts ./
COPY build.gradle.kts ./
COPY buildSrc ./buildSrc
RUN chmod +x ./gradlew && ./gradlew --version

# Копируем остальной проект
COPY . .
# Пара попыток на случай сетевых сбоев скачивания зависимостей
RUN bash -lc 'for i in 1 2 3; do ./gradlew :apps:monolith:bootJar --no-daemon --refresh-dependencies && exit 0 || (echo "Gradle failed, retry $i" && sleep 5); done; exit 1'

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /home/gradle/src/apps/monolith/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
