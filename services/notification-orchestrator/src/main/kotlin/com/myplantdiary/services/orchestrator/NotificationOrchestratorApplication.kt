package com.myplantdiary.services.orchestrator

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class NotificationOrchestratorApplication

fun main(args: Array<String>) {
    runApplication<NotificationOrchestratorApplication>(*args)
}
