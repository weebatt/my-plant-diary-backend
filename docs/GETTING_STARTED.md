# Быстрый старт

Этот документ поможет быстро поднять локальную среду и начать работу с проектом.

## Предпосылки
- JDK 21 (Temurin/Adoptium) или новее
- Docker и Docker Compose
- Git, Gradle Wrapper (`./gradlew` будет использоваться автоматически)

## Переменные окружения
- Скопируйте шаблон: `cp .env.example .env`
- Заполните значения при необходимости (пароли БД, токены внешних сервисов и т.д.).
- Docker Compose автоматически подхватит значения из `.env`.

## Запуск через Docker
1. Собрать образы и поднять окружение:
   - `docker compose build`
   - `docker compose up`
2. Проверка:
   - Gateway: `http://localhost:${GATEWAY_PORT:-8080}/healthz`
   - RabbitMQ UI: `http://localhost:15672` (guest/guest)
3. Остановка: `docker compose down`

## Альтернатива: запуск через Gradle (без Docker)
- Пример:
  - Gateway: `./gradlew :services:gateway:bootRun`
  - User: `./gradlew :services:user-service:bootRun`
- По умолчанию активен профиль `dev`. Авторизация отключена (`SECURITY_AUTH_ENABLED=false`).
- Чтобы включить JWT Resource Server, задайте:
  - `SECURITY_AUTH_ENABLED=true`
  - `SECURITY_JWT_ISSUER_URI=<issuer-uri>`

## Документация OpenAPI
- Включена Springdoc UI во всех сервисах (когда появятся контроллеры):
  - Gateway: `http://localhost:${GATEWAY_PORT:-8080}/swagger-ui/index.html`
  - User: `http://localhost:${USER_PORT:-8081}/swagger-ui/index.html`
  - Diary: `http://localhost:${DIARY_PORT:-8082}/swagger-ui/index.html`
  - Dictionary: `http://localhost:${DICTIONARY_PORT:-8083}/swagger-ui/index.html`
  - Orchestrator: `http://localhost:${ORCHESTRATOR_PORT:-8091}/swagger-ui/index.html`
  - Scheduler: `http://localhost:${SCHEDULER_PORT:-8092}/swagger-ui/index.html`
  - Telegram Adapter: `http://localhost:${TELEGRAM_PORT:-8093}/swagger-ui/index.html`
  - Avito Adapter: `http://localhost:${AVITO_PORT:-8094}/swagger-ui/index.html`

## Postman
- Коллекция и окружение:
  - `postman/MyPlantDiary.postman_collection.json`
  - `postman/local.postman_environment.json`
- Импортируйте оба файла в Postman, при необходимости заполните переменные окружения (порты, jwt и т.п.).

## Профили Spring
- `dev` (по умолчанию):
  - Быстрый локальный запуск, либеральная security (можно отключить авторизацию).
  - In-docker зависимости (RabbitMQ, PostgreSQL).
- `test`:
  - Для автотестов/CI. Переопределяйте порты и БД при необходимости.
- `prod`:
  - Жёсткая security, внешние зависимости, секреты только из окружения/секрет-сторов.

## Очистка
- Остановить и удалить контейнеры: `docker compose down`
- Удалить тома (если добавите volumes): `docker compose down -v`

## Git и конвенции коммитов
- Используем стиль Conventional Commits (ограниченный набор типов):
  - Формат: `тип(область)?: краткое_описание`
  - Допустимые типы: `feat`, `refactor`, `fix`, `chore`, `test`, `docs`, `ci`.
  - Область (scope) — по желанию, рекомендуем модуль/сервис: `gateway`, `user-service`, `contracts`, `ci`, `docs` и т.п.
  - Краткое описание — в повелительном наклонении, без точки на конце.
  - Body/Footers: детали изменений, `BREAKING CHANGE: ...` при несовместимости.
- Примеры:
  - `feat(gateway): add plants list endpoint`
  - `fix(user-service): correct nullable field mapping`
  - `ci: run detekt on pull request`
  - `docs: add architecture SOA diagram`

## Линтинг Kotlin (Detekt)
- Конфигурация детекта: `config/detekt/detekt.yml`
- Запуск локально: `./gradlew detekt`

## Git‑хуки
- Установка: `bash scripts/install-git-hooks.sh`
- commit-msg: проверяет формат сообщения коммита (Conventional Commits) — сообщение должно начинаться с одного из типов: `feat|refactor|fix|chore|test|docs`.
- pre-push: запускает `./gradlew detekt` и блокирует push при нарушениях.
