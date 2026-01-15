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
    private val tokenValidator: BindTokenValidator,
    @Value("\${telegram.botToken}") private val botToken: String,
    @Value("\${telegram.pollingDelayMs:2000}") private val delayMs: Long
) : ApplicationRunner {
    private val log = LoggerFactory.getLogger(PollingRunner::class.java)
    @Volatile private var lastUpdateId: Long = 0

    override fun run(args: ApplicationArguments) {
        log.info("Старт polling Telegram updates")
        while (true) {
            try {
                val url = URL("https://api.telegram.org/bot$botToken/getUpdates?offset=${lastUpdateId + 1}&timeout=25")
                val conn = url.openConnection() as HttpURLConnection
                conn.connectTimeout = 5000
                conn.readTimeout = 30000
                val body = conn.inputStream.bufferedReader().use { it.readText() }
                // Парсим блочно: находим update_id, chat.id и текст `/start <arg>` в одном совпадении
                val pattern = Regex(
                    """"update_id"\s*:\s*(\d+).*?"message".*?"chat"\s*:\s*\{[^}]*"id"\s*:\s*(\d+).*?"text"\s*:\s*"/start\s+([^\"]+)"""",
                    setOf(RegexOption.DOT_MATCHES_ALL)
                )
                pattern.findAll(body).forEach { m ->
                    val updId = m.groupValues[1].toLongOrNull()
                    val chatId = m.groupValues[2].toLongOrNull()
                    val arg = m.groupValues[3]
                    if (updId != null) lastUpdateId = maxOf(lastUpdateId, updId)
                    if (chatId != null && arg.isNotBlank()) {
                        val userId = if (arg.contains('.')) {
                            val res = tokenValidator.validate(arg)
                            if (res.ok) res.userId else null
                        } else arg
                        if (!userId.isNullOrBlank()) {
                            registry.put(userId, chatId)
                        }
                    }
                }
            } catch (e: Exception) {
                log.warn("Polling error", e)
            }
            Thread.sleep(delayMs)
        }
    }
}
