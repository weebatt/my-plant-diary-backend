package com.myplantdiary.apps.telegram.telegram

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.net.HttpURLConnection
import java.net.URL

@Component
@ConditionalOnProperty(prefix = "telegram", name = ["pollingEnabled"], havingValue = "true")
class PollingRunner(
    private val registry: ChatRegistry,
    @Value("\${telegram.botToken}") private val botToken: String,
    @Value("\${telegram.pollingDelayMs:2000}") private val delayMs: Long
) : ApplicationRunner {
    private val log = LoggerFactory.getLogger(PollingRunner::class.java)
    @Volatile private var lastUpdateId: Long = 0

    override fun run(args: ApplicationArguments) {
        log.info("Старт polling Telegram updates")
        while (true) {
            try {
                val url = URL("https://api.telegram.org/bot$botToken/getUpdates?offset=${lastUpdateId + 1}")
                val conn = url.openConnection() as HttpURLConnection
                conn.connectTimeout = 5000
                conn.readTimeout = 5000
                val text = conn.inputStream.bufferedReader().use { it.readText() }
                // очень упрощённый парсинг: ищем `/start <userId>` и chat.id
                val updates = text.lines().filter { it.contains("/start ") && it.contains("chat") }
                updates.forEach { line ->
                    val chatId = Regex(""""id":(\d+)""").find(line)?.groupValues?.get(1)?.toLongOrNull()
                    val userId = Regex("""/start ([a-zA-Z0-9-]+)""").find(line)?.groupValues?.get(1)
                    val updId = Regex(""""update_id":(\d+)""").find(line)?.groupValues?.get(1)?.toLongOrNull()
                    if (chatId != null && !userId.isNullOrBlank()) {
                        registry.put(userId, chatId)
                    }
                    if (updId != null) lastUpdateId = maxOf(lastUpdateId, updId)
                }
            } catch (e: Exception) {
                log.warn("Polling error", e)
            }
            Thread.sleep(delayMs)
        }
    }
}

