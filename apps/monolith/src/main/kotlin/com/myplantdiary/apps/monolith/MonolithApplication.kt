package com.myplantdiary.apps.monolith

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@SpringBootApplication
class MonolithApplication

fun main(args: Array<String>) {
    runApplication<MonolithApplication>(*args)
}

@RestController
class HealthController {
    @GetMapping("/healthz")
    fun health() = mapOf("status" to "ok")
}

