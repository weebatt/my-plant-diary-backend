package com.myplantdiary.apps.monolith.messaging

import com.rabbitmq.client.Channel
import events.EventEnvelope
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import org.slf4j.LoggerFactory
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.messaging.handler.annotation.Header
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(prefix = "app.messaging", name = ["enabled"], havingValue = "true")
class MessagingConsumer {
    private val log = LoggerFactory.getLogger(MessagingConsumer::class.java)
    private val json = KotlinxJson.json

    @RabbitListener(
        queues = ["#{eventsQueue.name}"],
        containerFactory = "manualAckListenerContainerFactory"
    )
    fun onEvent(
        message: Message,
        channel: Channel,
        @Header(name = "amqp_receivedRoutingKey", required = false) routingKey: String?
    ) {
        val deliveryTag = message.messageProperties.deliveryTag
        try {
            val body = message.body.toString(Charsets.UTF_8)
            val envelope = json.decodeFromString(EventEnvelope.serializer(JsonElement.serializer()), body)
            log.info(
                "Received event: type={} v{} rk={} id={} payloadKeys={}",
                envelope.type,
                envelope.version,
                routingKey,
                envelope.id,
                (envelope.payload as? JsonElement)?.jsonObject?.keys?.joinToString(",")
            )
            channel.basicAck(deliveryTag, false)
        } catch (ex: Exception) {
            log.error("Failed to process message, nacking to DLQ", ex)
            channel.basicNack(deliveryTag, false, false)
        }
    }
}
