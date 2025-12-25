package com.myplantdiary.apps.monolith.api

import com.myplantdiary.apps.monolith.messaging.MessagingPublisherService
import events.EventEnvelope
import events.ReminderDue
import kotlinx.datetime.Clock
import kotlinx.serialization.builtins.serializer
import io.swagger.v3.oas.annotations.Operation
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/dev")
@ConditionalOnProperty(prefix = "app.messaging", name = ["enabled"], havingValue = "true")
class DevMessagingController(
    private val publisher: MessagingPublisherService
) {
    @GetMapping("/emit/reminder")
    @Operation(summary = "Опубликовать тестовое событие напоминания в брокер (dev)")
    fun emitReminder(): Map<String, String> {
        val id = UUID.randomUUID().toString()
        val envelope = EventEnvelope(
            id = id,
            type = "notification.reminder.due",
            version = "1",
            occurredAt = Clock.System.now(),
            payload = ReminderDue(
                reminderId = "rem-$id",
                userId = "user-1",
                dueAtEpochMs = System.currentTimeMillis() + 1000
            )
        )
        publisher.publishEvent(envelope, ReminderDue.serializer())
        return mapOf("status" to "published", "id" to id)
    }
}
