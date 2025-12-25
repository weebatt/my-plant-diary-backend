# My Plant Diary — Backend

[Быстрый старт (Getting Started)](docs/GETTING_STARTED.md)

## Обзор
Репозиторий переведён на монолитную архитектуру:
- Монолитное приложение на Spring Boot: `apps/monolith` (HTTP API, Actuator).
- Общие контракты (DTO/события): `contracts`.

Исторические мультисервисные модули (SOA) удалены из сборки и документации.

## Архитектура
- Монолит: единое приложение (`apps/monolith`) с REST/API и возможностью постепенного наращивания доменов (user/diary/dictionary и т.п.).
- Диаграмма и пояснения: `ARCHITECTURE.md:1` (схема монолита)

## План реализации
- План этапов и журнал прогресса: `docs/IMPLEMENTATION_PLAN.md:1`

## Модули
- Monolith
  - `apps/monolith` — основное приложение (REST/API, Actuator). Включает лёгкие обёртки для AMQP‑паблиша и listener‑фабрики.

- Contracts
  - `contracts` — общие DTO/события (kotlinx‑serialization)

## Технологии и зависимости
- Spring Boot (Web, Actuator, AMQP)
- RabbitMQ (Spring AMQP) — опционально; конфиг присутствует для совместимости
- Detekt (линтинг Kotlin)
- Version Catalog: `gradle/libs.versions.toml:1`

## Профили и конфигурация
- Активный профиль: `SPRING_PROFILES_ACTIVE` (по умолчанию `dev`).
- Секреты — через env (RabbitMQ и др.).
- Переключение авторизации: `SECURITY_AUTH_ENABLED` (по умолчанию `false`).
 - JWT параметры: `SECURITY_JWT_ISSUER`, `SECURITY_JWT_AUDIENCE`, `SECURITY_JWT_SECRET`.

## Принятые решения (актуально сейчас)
- Messaging: RabbitMQ (Spring AMQP). См. `docs/messaging.md:1` (формат, топология, конвенции).
- HTTP: REST (Spring Web) + Springdoc OpenAPI во всех сервисах.
- Persistence: Spring Data JPA + Liquibase (db/changelog per service), PostgreSQL.
- Graph/Triple Store: RDF4J (подключено в dictionary-service).
- Security: OAuth2 / JWT Resource Server (подготовлено, управляется переменными окружения).

## Postman
- Коллекция и окружение: `postman/MyPlantDiary.postman_collection.json`, `postman/local.postman_environment.json`.

## Docker
- Локальный стек (RabbitMQ + Postgres + Monolith): `docker compose up --build`
- RabbitMQ UI: `http://localhost:${RABBITMQ_MANAGEMENT_PORT:-15672}` (guest/guest по умолчанию)

## CI/CD
- GitHub Actions: `.github/workflows/ci.yml:1` — Detekt + Gradle build на push/PR.

## Линтинг и стиль (Detekt)
- Конфигурация: `config/detekt/detekt.yml` (нейминг, форматирование).
- Запуск: `./gradlew detekt`.

## Git‑хуки
- Установка хуков: `bash scripts/install-git-hooks.sh`
- commit-msg: проверяет, что сообщение коммита начинается с одного из типов `feat|refactor|fix|chore|test|docs` по конвенции (`type(scope)?: subject`).
- pre-push: запускает `./gradlew detekt` и блокирует push при нарушениях стиля/правил.

## Git‑конвенции (коммиты)
- Принимаем стиль Conventional Commits (ограниченный набор типов):
  - Формат: `тип(область)?: краткое_описание`
  - Допустимые типы: `feat`, `refactor`, `fix`, `chore`, `test`, `docs`, `ci`.
  - Область (scope) — рекомендуется указывать сервис/модуль: `gateway`, `user-service`, `contracts`, `ci`, `docs` и т.д.
  - Сообщение — в повелительном наклонении, без точки в конце.
  - Дополнительно: подробности в теле; `BREAKING CHANGE:` в футере, если есть несовместимость.
- Примеры:
  - `feat(diagram): update PlantUML for SOA`
  - `fix(dictionary-service): init RDF4J repository config`
  - `ci: add commit message validation to CI`

## Документация API (OpenAPI)
- Доступ к Swagger UI после появления контроллеров.
- При включённой авторизации доступны:
  - `POST /auth/register` (всегда открыт)
  - `POST /auth/login` (в JWT‑режиме)
  - `GET /me`, `GET /profile`, `PATCH /profile`, `POST /profile/password` (требуют JWT)
- Публичные (без JWT): `GET /plants`, `GET /plants/{id}`, `GET /plants/recommendations`
 - Календарь (JWT): `GET /calendar/day?date=YYYY-MM-DD`, `GET /calendar/week?start=YYYY-MM-DD`, `GET /calendar/month?year=YYYY&month=MM` (+ `page,size,sort`)

## Быстрые примеры (curl)
- Публичные растения:
  - `curl "http://localhost:8080/plants?q=ficus&page=0&size=5&sort=-latinName"`
- JWT режим:
  - Логин админа: `curl -XPOST -H 'Content-Type: application/json' -d '{"email":"admin@example.com","password":"admin123"}' http://localhost:8080/auth/login`
  - Добавить растение пользователя: `curl -XPOST -H "Authorization: Bearer <JWT>" -H 'Content-Type: application/json' -d '{"nickname":"Мой фикус"}' http://localhost:8080/diary/plants`
  - Создать напоминание: `curl -XPOST -H "Authorization: Bearer <JWT>" -H 'Content-Type: application/json' -d '{"kind":"water","dueAt":"2030-01-01T10:00:00Z"}' http://localhost:8080/diary/reminders/<userPlantId>`
  - Календарь (день): `curl -H "Authorization: Bearer <JWT>" "http://localhost:8080/calendar/day?date=2030-01-01&page=0&size=50&sort=dueAt"`

## Безопасность и секреты
- Секреты не хардкодим. Используем переменные окружения/секрет‑сторы.
- Docker Compose читает значения из `.env`. Шаблон: `.env.example:1`.

## Дальнейшие шаги
- Расширение API и доменных модулей (M1–M3).
- Миграции Liquibase и подключение PostgreSQL (M1+).
- Интеграционные тесты с Testcontainers (M5).
