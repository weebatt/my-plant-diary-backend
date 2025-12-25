package com.myplantdiary.apps.telegram.telegram

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class TelegramUpdate(val message: TelegramMessage?)
data class TelegramMessage(val chat: TelegramChat, val text: String?)
data class TelegramChat(val id: Long)

@RestController
@RequestMapping("/telegram")
@ConditionalOnProperty(prefix = "telegram", name = ["webhookEnabled"], havingValue = "true")
class TelegramWebhookController(
    private val registry: ChatRegistry
) {
    private val log = LoggerFactory.getLogger(TelegramWebhookController::class.java)

    @PostMapping("/webhook")
    fun onWebhook(@RequestBody update: TelegramUpdate) {
        val msg = update.message ?: return
        val text = msg.text ?: return
        // Команда `/start <userId>` для привязки chatId к userId
        if (text.startsWith("/start ")) {
            val userId = text.removePrefix("/start ").trim()
            if (userId.isNotBlank()) {
                registry.put(userId, msg.chat.id)
            }
        }
    }
}

