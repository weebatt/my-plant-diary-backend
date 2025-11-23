package com.myplantdiary.services.outbox

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class OutboxPublisherApplication

fun main(args: Array<String>) {
    runApplication<OutboxPublisherApplication>(*args)
}
