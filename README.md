# My Plant Diary — Backend

[Быстрый старт (Getting Started)](docs/GETTING_STARTED.md)

## Обзор
Мультисервисный backend (SOA) для MyPlantDiary. Сервисы общаются по REST и через брокер сообщений (RabbitMQ). Хранилища — PostgreSQL (per-service) и опционально Graph/Triple Store (RDF4J) для словаря растений. В качестве фреймворка используется Spring Boot. Общая сборочная логика — в `buildSrc`.

## Архитектура
- Диаграмма и пояснения: `ARCHITECTURE.md:1` (PlantUML)
- Основные элементы: Gateway, User/Diary/Dictionary, Orchestrator, Scheduler, Outbox Publisher, Adapter‑сервисы (Telegram, Avito, WebPush), RabbitMQ, PostgreSQL per‑service, RDF4J.

## Модули
- Contracts
  - `contracts` — общие DTO/события (kotlinx-serialization)

- Services (отдельные процессы)
  - `services/gateway` — HTTP API + WS/SSE
  - `services/user-service` — пользовательский домен
  - `services/diary-service` — домен дневника
  - `services/dictionary-service` — словарь растений
  - `services/notification-orchestrator` — маршрутизация уведомлений
  - `services/scheduler` — планирование ReminderDue
  - `services/outbox-publisher` — публикация событий из outbox
  - `services/adapters/telegram-service` — интеграция Telegram
  - `services/adapters/avito-service` — интеграция Avito (объявления)
  - `services/adapters/webpush-service` — интеграция Web Push

- Infrastructure (общие клиенты/библиотеки)
  - `infra/broker` — абстракции брокера
  - `infra/stores/postgres` — доступ к PostgreSQL
  - `infra/stores/graphdb` — доступ к графовому/трипл стору

Примечание: шаблонные модули `app` и `core/*` удалены в пользу сервисной архитектуры.

## Технологии и зависимости
- Spring Boot (Web, Actuator, Security, OAuth2 Resource Server, AMQP, Data JPA)
- RabbitMQ (Spring AMQP)
- PostgreSQL, Liquibase (миграции per‑service)
- RDF4J (словарь/таксономия)
- Springdoc OpenAPI (UI + OpenAPI)
- Detekt (линтинг Kotlin)
- Version Catalog: `gradle/libs.versions.toml:1`

## Профили и конфигурация
- Активный профиль: `SPRING_PROFILES_ACTIVE` (по умолчанию `dev`).
- Секреты — через env (RabbitMQ, доступ к БД, JWT issuer и т.п.).
- Конфиг каждого сервиса: `application.yml`, переопределения — `application-*.yml`.
- Переключение авторизации: `SECURITY_AUTH_ENABLED` (по умолчанию `false`), при `true` нужен `SECURITY_JWT_ISSUER_URI`.

## Принятые решения (актуально сейчас)
- Messaging: RabbitMQ (Spring AMQP). Топология уточним позже.
- HTTP: REST (Spring Web) + Springdoc OpenAPI во всех сервисах.
- Persistence: Spring Data JPA + Liquibase (db/changelog per service), PostgreSQL.
- Graph/Triple Store: RDF4J (подключено в dictionary-service).
- Security: OAuth2 / JWT Resource Server (подготовлено, управляется переменными окружения).

## Postman
- Коллекция и окружение: `postman/MyPlantDiary.postman_collection.json`, `postman/local.postman_environment.json`.

## Docker
- Локальный запуск: см. [Быстрый старт](docs/GETTING_STARTED.md)

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
- Доступ к Swagger UI после появления контроллеров (см. адреса в [Быстрый старт](docs/GETTING_STARTED.md)).

## Безопасность и секреты
- Секреты не хардкодим. Используем переменные окружения/секрет‑сторы.
- Docker Compose читает значения из `.env`. Шаблон: `.env.example:1`.

## Дальнейшие шаги
- Формат сообщений (JSON/Avro/Proto) и топология RabbitMQ.
- Миграции Liquibase и API контракты во всех сервисах.
