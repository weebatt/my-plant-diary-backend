# Сообщения: формат и топология RabbitMQ

Этот документ задаёт простой и практичный базовый стандарт обмена сообщениями между сервисами My Plant Diary.

## Принятые решения
- Формат сообщения: JSON с использованием Kotlin `kotlinx-serialization` и общего конверта `events.EventEnvelope<T>` из модуля `contracts`.
- Брокер: RabbitMQ (AMQP 0-9-1), обменники/очереди — durable, сообщения — persistent.
- Топология: два основных обменника — `x.events` (topic) и `x.commands` (direct) — плюс dead‑letter обменник `x.dlx` (topic). По одной очереди событий и одной очереди команд на сервис.
- Версионирование: при добавлении полей сохраняем `type` и `version`; при ломающих изменениях повышаем `version` и суффикс ключа маршрутизации до `v{N}`.
- Повторные попытки: начинаем просто — ретраи в коде (экспоненциальная задержка) до N попыток, затем NACK в DLQ. Брокерные задержанные ретраи добавим при необходимости.
- Синхронные вызовы: предпочитаем HTTP для синхронных потоков; RPC поверх RabbitMQ пока не используем.

## Формат сообщения
- Конверт (уже есть в `contracts/src/main/kotlin/events/EventEnvelope.kt`):
  - `id: String` — UUID (рекомендуется v7) для идемпотентности и трейсинга.
  - `type: String` — логический тип через точки, напр. `listing.publish.requested`.
  - `version: String` — версия схемы полезной нагрузки, напр. `1`.
  - `occurredAt: Instant` — ISO‑8601 момент формирования.
  - `payload: T` — тело события/команды (JSON через `kotlinx`).
- Заголовки (AMQP message properties):
  - `content-type: application/json`
  - `message-id`: копия `envelope.id`
  - `correlation-id` (опционально): связь с бизнес‑потоком/запросом.
  - `traceparent` (опционально): W3C Trace Context при включённом трейсинге.
  - `x-producer`: имя сервиса‑производителя.

## Маршрутизация и наименования
- Routing key = `${type}.v${version}` (например, `listing.publish.requested.v1`).
- Обменники:
  - `x.events` (тип `topic`, durable): для доменных событий (fan‑out по шаблонам).
  - `x.commands` (тип `direct`, durable): для команд point‑to‑point целевому сервису.
  - `x.dlx` (тип `topic`, durable): dead‑letter обменник (DLX) для всех очередей.
- Очереди (на сервис):
  - События: `q.<service>.events` (DLQ: `q.<service>.events.dlq`).
  - Команды: `q.<service>.commands` (DLQ: `q.<service>.commands.dlq`).
- Привязки:
  - События: привязать `q.<service>.events` к `x.events` с нужными шаблонами (напр., `notification.#`, `listing.#` или конкретные `*.v1`).
  - Команды: привязать `q.<service>.commands` к `x.commands` с ключом `<service>`.
  - DLQ: каждая `*.dlq` служит целевой очередью для dead‑letter и (при необходимости) может быть привязана к `x.dlx`.

## Минимальная обработка ошибок/ретраев
- Консьюмеры используют ручные подтверждения (manual acks). При временных ошибках — ретраи в коде (например, 3 попытки с экспоненциальной задержкой). При окончательной неудаче — `basicNack(requeue=false)`, чтобы сообщение попало в DLQ через `x.dlx`.
- Обработка DLQ: мониторинг и ручной/утилитой реплей; при росте потребности — добавить брокерные задержанные ретраи.

## Семантика доставки
- At‑least‑once. Консьюмеры идемпотентны, де‑дупликация по `envelope.id`.
- Сообщения persistent (`deliveryMode=2`), обменники/очереди durable, публикация с подтверждениями (publisher confirms).
- Устанавливать `prefetch` на разумное значение (напр., 20–50) на инстанс консьюмера.

## Рекомендуемые права доступа
- Отдельный vhost на окружение: `mpd-dev`, `mpd-stg`, `mpd-prod`.
- Один пользователь RabbitMQ на сервис с минимально необходимыми правами:
  - запись в `x.events` и/или `x.commands`, если сервис публикует;
  - чтение/consume только своих очередей; без доступа к чужим.

## Примеры типов и routing keys
- Домен уведомлений:
  - Событие `notification.reminder.due.v1` → `ReminderDue`
  - Команда `notification.channel.send.v1` → `ChannelSend`
  - События `notification.sent.v1` / `notification.failed.v1` → `NotificationSent` / `NotificationFailed`
- Домен объявлений:
  - Команда `listing.publish.requested.v1` → `ListingPublishRequested`
  - Событие `listing.published.v1` → `ListingPublished`
  - Событие `listing.publish.failed.v1` → `ListingPublishFailed`

## Примеры JSON

Событие: `notification.reminder.due.v1`, публикуется в `x.events` с routing key `notification.reminder.due.v1`

```json
{
  "id": "018f8b74-4c86-7b7a-b4f0-9a0fca9f8c01",
  "type": "notification.reminder.due",
  "version": "1",
  "occurredAt": "2025-01-15T08:30:00Z",
  "payload": {
    "reminderId": "rem-123",
    "userId": "user-42",
    "dueAtEpochMs": 1736920200000
  }
}
```

Команда: `notification.channel.send.v1`, публикуется в `x.commands` с routing key `telegram-adapter`

```json
{
  "id": "018f8b74-4c8a-7cba-8c20-2b2b87f1c9e0",
  "type": "notification.channel.send",
  "version": "1",
  "occurredAt": "2025-01-15T08:30:01Z",
  "payload": {
    "channel": "telegram",
    "correlationId": "corr-abc-123",
    "userId": "user-42",
    "content": "Полить растение Антуриум сегодня"
  }
}
```

Событие: `listing.published.v1`, публикуется в `x.events` с routing key `listing.published.v1`

```json
{
  "id": "018f8b74-4c8b-7e3c-9dfe-67aa12a34567",
  "type": "listing.published",
  "version": "1",
  "occurredAt": "2025-01-15T10:00:00Z",
  "payload": {
    "listingId": "lst-12",
    "externalId": "avito-987654"
  }
}
```

## Объявление в Spring AMQP (набросок)

Объявляем обменники и очереди в каждом сервисе (схематично, Kotlin):

```kotlin
@Bean fun eventsExchange() = TopicExchange("x.events", true, false)
@Bean fun commandsExchange() = DirectExchange("x.commands", true, false)
@Bean fun dlxExchange() = TopicExchange("x.dlx", true, false)

@Bean fun serviceEventsQueue() = Queue("q.<service>.events", true, false, false, mapOf(
  "x-dead-letter-exchange" to "x.dlx"
))

@Bean fun serviceCommandsQueue() = Queue("q.<service>.commands", true, false, false, mapOf(
  "x-dead-letter-exchange" to "x.dlx"
))

@Bean fun eventsBinding() = BindingBuilder
  .bind(serviceEventsQueue())
  .to(eventsExchange())
  .with("<pattern>")

@Bean fun commandsBinding() = BindingBuilder
  .bind(serviceCommandsQueue())
  .to(commandsExchange())
  .with("<service>")
```

Примечания:
- Используем ручные подтверждения и настраиваем `prefetch` (например, 20–50) на консьюмер.
- Включаем publisher confirms и ставим `deliveryMode=2` при публикации.
- Сериализация `EventEnvelope<T>` — через `kotlinx-serialization`.

## Правила версионирования
- Добавление полей: `version` не меняется; консьюмеры должны игнорировать неизвестные поля.
- Ломающие изменения: повышаем `version` и суффикс ключа маршрутизации до `.v{N}`; старые консьюмеры держим до миграции.
- Депрекейты: сохраняем предыдущую версию на безопасный период; при необходимости эмитим обе.

## Когда стоит вернуться к Avro/Proto
- Если потребуется бинарная эффективность, строгая совместимость (backward/forward) или развитая межъязыковая схема‑инфраструктура — рассмотреть Avro или Proto. Пока что JSON + DTO в `contracts` — самый простой и подходящий для Spring + Kotlin вариант.
