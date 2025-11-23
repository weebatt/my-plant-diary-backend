package com.myplantdiary.services.adapters.telegram

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TelegramAdapterApplication

fun main(args: Array<String>) {
    runApplication<TelegramAdapterApplication>(*args)
}
