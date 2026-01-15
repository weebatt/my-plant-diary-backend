package com.myplantdiary.apps.monolith.notifications

import com.myplantdiary.apps.monolith.diary.Reminder
import com.myplantdiary.apps.monolith.diary.ReminderRepository
import com.myplantdiary.apps.monolith.diary.UserPlantRepository
import com.myplantdiary.apps.monolith.dictionary.PlantRepository
import com.myplantdiary.apps.monolith.messaging.KotlinxJson
import com.myplantdiary.apps.monolith.infra.BrokerPublisher
import com.myplantdiary.apps.monolith.infra.PublishSpec
import events.ChannelSend
import events.EventEnvelope
import kotlinx.datetime.Clock
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID

@Component
@EnableScheduling
@ConditionalOnProperty(prefix = "app.notifications", name = ["schedulerEnabled"], havingValue = "true")
class ReminderNotificationScheduler(
    @Value("\${app.timezone:system}") private val appTimezone: String,
    private val reminders: ReminderRepository,
    private val brokerPublisher: BrokerPublisher,
    private val userPlants: UserPlantRepository,
    private val plants: PlantRepository,
) {
    private val log = LoggerFactory.getLogger(ReminderNotificationScheduler::class.java)
    private val json = KotlinxJson.json

    // Каждую минуту ищем просроченные неуведомленные напоминания и публикуем команды на телеграм‑адаптер
    @Scheduled(fixedDelayString = "\${app.notifications.schedulerIntervalMs:60000}")
    fun tick() {
        val now = OffsetDateTime.now(ZoneOffset.UTC)
        val due = reminders.findAllByCompletedFalseAndNotifiedFalseAndDueAtBefore(now)
        if (due.isEmpty()) return
        log.info("Scheduler: found {} due reminders to notify", due.size)
        due.forEach { r ->
            try {
                publishTelegram(r)
                // помечаем как уведомлённые (без сложных ретраев покамест)
                val updated = r.copy(notified = true, updatedAt = now)
                reminders.save(updated)
            } catch (e: Exception) {
                log.warn("Failed to publish notification for reminder {}", r.id, e)
            }
        }
    }

    private fun publishTelegram(r: Reminder) {
        val content = buildContent(r)
        val env = EventEnvelope(
            id = UUID.randomUUID().toString(),
            type = "notification.channel.send",
            version = "1",
            occurredAt = Clock.System.now(),
            payload = ChannelSend(
                channel = "telegram",
                correlationId = "reminder-${r.id}",
                userId = r.userId.toString(),
                content = content
            )
        )
        val body = json.encodeToString(EventEnvelope.serializer(ChannelSend.serializer()), env).toByteArray()
        brokerPublisher.publish(
            PublishSpec(
                exchange = "x.commands",
                routingKey = "telegram-adapter",
                body = body,
                messageId = env.id,
                producer = "monolith"
            )
        )
    }

    private fun buildContent(r: Reminder): String {
        val kind = r.kind.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        val zone = runCatching {
            if (appTimezone.equals("system", ignoreCase = true)) ZoneId.systemDefault() else ZoneId.of(appTimezone)
        }.getOrElse { ZoneId.systemDefault() }
        val whenLocal = r.dueAt.toInstant().atZone(zone)
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm z"))
        val plantTitle = resolvePlantTitle(r)
        val notesPart = r.notes?.let { "\nNotes: $it" } ?: ""
        return "Reminder: $kind\nPlant: $plantTitle\nWhen: $whenLocal$notesPart"
    }

    private fun resolvePlantTitle(r: Reminder): String {
        return try {
            val up = userPlants.findById(r.userPlantId).orElse(null) ?: return r.userPlantId.toString()
            val nick = up.nickname?.takeIf { it.isNotBlank() }
            val plantName = up.plantId?.let { id ->
                plants.findById(id).map { it.commonName ?: it.latinName }.orElse(null)
            }
            when {
                nick != null && plantName != null -> "$nick ($plantName)"
                nick != null -> nick
                plantName != null -> plantName
                else -> r.userPlantId.toString()
            }
        } catch (_: Exception) {
            r.userPlantId.toString()
        }
    }
}
