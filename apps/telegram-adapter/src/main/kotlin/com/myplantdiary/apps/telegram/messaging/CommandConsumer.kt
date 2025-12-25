package com.myplantdiary.apps.telegram.messaging

import com.myplantdiary.apps.telegram.infra.BrokerPublisher
import com.myplantdiary.apps.telegram.telegram.TelegramBotService
import com.rabbitmq.client.Channel
import events.ChannelSend
import events.EventEnvelope
import events.NotificationFailed
import events.NotificationSent
import kotlinx.datetime.Clock
import kotlinx.serialization.json.JsonElement
import org.slf4j.LoggerFactory
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.beans.factory.annotation.Value
import org.springframework.messaging.handler.annotation.Header
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class CommandConsumer(
    private val telegram: TelegramBotService,
    private val publisher: MessagingPublisherService,
    @Value("\${spring.application.name}") private val appName: String,
) {
    private val log = LoggerFactory.getLogger(CommandConsumer::class.java)
    private val json = KotlinxJson.json

    @RabbitListener(
        queues = ["#{commandsQueue.name}"],
        containerFactory = "manualAckListenerContainerFactory"
    )
    fun onCommand(
        message: Message,
        channel: Channel,
        @Header(name = "amqp_receivedRoutingKey", required = false) routingKey: String?
    ) {
        val tag = message.messageProperties.deliveryTag
        try {
            val body = message.body.toString(Charsets.UTF_8)
            val env = json.decodeFromString(EventEnvelope.serializer(JsonElement.serializer()), body)
            if (env.type == "notification.channel.send") {
                val send = json.decodeFromString(EventEnvelope.serializer(ChannelSend.serializer()), body)
                if (send.payload.channel.equals("telegram", ignoreCase = true)) {
                    val ok = telegram.sendToUser(send.payload.userId, send.payload.content)
                    if (ok) publishSent(send.payload.correlationId, "telegram") else publishFailed(send.payload.correlationId, "telegram", "send error")
                } else {
                    log.debug("Skip non-telegram channel: {}", send.payload.channel)
                }
            } else {
                log.debug("Skip command type {} rk {}", env.type, routingKey)
            }
            channel.basicAck(tag, false)
        } catch (e: Exception) {
            log.error("Failed to process command, nacking", e)
            channel.basicNack(tag, false, false)
        }
    }

    private fun publishSent(correlationId: String, channel: String) {
        val env = EventEnvelope(
            id = UUID.randomUUID().toString(),
            type = "notification.sent",
            version = "1",
            occurredAt = Clock.System.now(),
            payload = NotificationSent(correlationId = correlationId, channel = channel)
        )
        publisher.publishEvent(env, NotificationSent.serializer())
    }

    private fun publishFailed(correlationId: String, channel: String, reason: String) {
        val env = EventEnvelope(
            id = UUID.randomUUID().toString(),
            type = "notification.failed",
            version = "1",
            occurredAt = Clock.System.now(),
            payload = NotificationFailed(correlationId = correlationId, channel = channel, reason = reason)
        )
        publisher.publishEvent(env, NotificationFailed.serializer())
    }
}

