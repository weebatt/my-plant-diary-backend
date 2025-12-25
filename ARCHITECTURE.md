@startuml
title MyPlantDiary — монолитная архитектура

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
component "Monolith\n(Spring Boot)" as Monolith #E8F5E9

' ======================== ИНФРАСТРУКТУРА ========================
package "Инфраструктура" as Infra #FFF3E0 {
  component "RabbitMQ" as Broker
  database "PostgreSQL" as PG
}

' ======================== ВНЕШНИЕ СИСТЕМЫ ========================
package "Внешние системы" as Ext #F3E5F5 {
  component "Telegram\nPlatform" as Telegram
  component "Avito\nMarketplace" as Avito
  component "Web Push\nService" as WebPushExt
}

' ======================== СВЯЗИ ========================

' Клиенты ↔ Gateway
Clients -right-> Monolith

note right of Gateway
Внешний вход: REST/HTTP и WS/SSE для клиентов.
Внутренние вызовы к доменным сервисам по HTTP/gRPC.
end note

Monolith -right-> Broker : ReminderDue, Channel.Send.*,\nNotification{Sent|Failed}
Monolith -down-> PG : domain data + outbox

' Контракты
note top of Broker
Контракты событий/DTO — `contracts`.
end note

@enduml
