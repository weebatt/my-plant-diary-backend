package com.myplantdiary.apps.telegram.telegram

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

@Service
class TelegramBotService(
    private val registry: ChatRegistry,
    @Value("\${telegram.botToken}") private val botToken: String
) {
    private val log = LoggerFactory.getLogger(TelegramBotService::class.java)

    fun sendToUser(userId: String, text: String): Boolean {
        val chatId = registry.get(userId) ?: run {
            log.warn("Нет chatId для userId={}, сообщение не отправлено", userId)
            return false
        }
        return sendMessage(chatId, text)
    }

    fun sendMessage(chatId: Long, text: String): Boolean {
        return try {
            val api = "https://api.telegram.org/bot$botToken/sendMessage" +
                "?chat_id=$chatId&text=" + URLEncoder.encode(text, "UTF-8")
            val conn = URL(api).openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 5000
            conn.readTimeout = 5000
            conn.inputStream.use { }
            conn.responseCode in 200..299
        } catch (e: Exception) {
            log.error("Ошибка отправки в Telegram", e)
            false
        }
    }
}

