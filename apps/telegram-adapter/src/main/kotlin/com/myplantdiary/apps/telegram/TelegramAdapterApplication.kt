package com.myplantdiary.apps.telegram

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@SpringBootApplication
class TelegramAdapterApplication

fun main(args: Array<String>) {
    runApplication<TelegramAdapterApplication>(*args)
}

@RestController
class HealthController {
    @GetMapping("/healthz")
    fun health() = mapOf("status" to "ok")
}

