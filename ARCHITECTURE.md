@startuml
title MyPlantDiary — сервисная архитектура (SOA)

skinparam componentStyle rectangle
skinparam packageStyle rectangle
skinparam rectangle {
  RoundCorner 12
  Shadowing true
}

left to right direction

' ======================== КЛИЕНТЫ ========================
package "Клиенты" as Clients #E3F2FD {
  component "Web\nFrontend" as WebClient
  component "Admin\nConsole" as Admin
}

' ======================== СЕРВИСЫ (DEPLOYABLES) ========================
package "Сервисы" as Svcs #E8F5E9 {
  component "Gateway\n(HTTP + WS/SSE)" as Gateway
  component "User\nService" as UserSvc
  component "Diary\nService" as DiarySvc
  component "Plant Dictionary\nService" as DictSvc
  component "Notification\nOrchestrator" as Orchestrator
  component "Scheduler" as Scheduler
  component "Outbox\nPublisher" as Outbox

  package "Adapters (services)" as AdapterSvcs {
    component "Telegram\nService" as TgSvc
    component "Avito\nService" as AvitoSvc
    component "WebPush\nService" as WebPushSvc
  }
}

' ======================== ИНФРАСТРУКТУРА ========================
package "Инфраструктура" as Infra #FFF3E0 {
  component "Message Broker\n(RabbitMQ/NATS)" as Broker
  package "Хранилища" as Stores {
    database "PostgreSQL\n(per-service schemas/DBs)" as PG
    database "Graph/Triple Store\n(optional)" as GraphDB
  }
}

' ======================== ВНЕШНИЕ СИСТЕМЫ ========================
package "Внешние системы" as Ext #F3E5F5 {
  component "Telegram\nPlatform" as Telegram
  component "Avito\nMarketplace" as Avito
  component "Web Push\nService" as WebPushExt
}

' ======================== СВЯЗИ ========================

' Клиенты ↔ Gateway
Clients -right-> Gateway

note right of Gateway
Внешний вход: REST/HTTP и WS/SSE для клиентов.
Внутренние вызовы к доменным сервисам по HTTP/gRPC.
end note

' Gateway ↔ Доменные сервисы (синхронно)
Gateway -down-> UserSvc : HTTP/gRPC
Gateway -down-> DiarySvc : HTTP/gRPC
Gateway -down-> DictSvc : HTTP/gRPC

' Сервисы ↔ Хранилища (каждый к своей схеме/БД)
Svcs -down-> Stores : per-service data/outbox

note right of Stores
Каждый сервис владеет собственными данными
(отдельная БД или схема). Кросс-сервисных JOIN нет.
Outbox реализуется в сервисах, публикующих события.
end note

' Сервисы ↔ Broker (события/команды)
Svcs -right-> Broker : ReminderDue, Channel.Send.*,\nNotification{Sent|Failed}, internal.status

note bottom of Svcs
Scheduler планирует ReminderDue. Outbox Publisher
читает outbox из своей БД и публикует события.
Orchestrator выбирает канал/время и эмитит Channel.Send.*.
end note

' Adapter services ↔ Broker
AdapterSvcs -up-> Broker : Channel.Send.* \n↔ Notification{Sent|Failed}

' Adapter services ↔ Внешние системы
AdapterSvcs -right-> Ext : HTTPS API / Web Push

note right of AdapterSvcs
Telegram Service — long polling/updates API.
Avito Service — публикация объявлений о растениях.
WebPush Service — отправка push-уведомлений.
end note

' Контракты
note top of Broker
Контракты событий/DTO версионируются (см. `contracts`).
Совместимость обеспечивается консьюмер-тестами.
end note

@enduml
