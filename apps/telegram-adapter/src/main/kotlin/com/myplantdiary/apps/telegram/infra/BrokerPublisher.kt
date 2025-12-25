package com.myplantdiary.apps.telegram.infra

import org.springframework.amqp.core.Message
import org.springframework.amqp.core.MessageBuilder
import org.springframework.amqp.core.MessageDeliveryMode
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

data class PublishSpec(
    val exchange: String,
    val routingKey: String,
    val body: ByteArray,
    val messageId: String,
    val correlationId: String? = null,
    val producer: String? = null,
    val contentType: String = "application/json"
)

@ConditionalOnProperty(prefix = "app.messaging", name = ["enabled"], havingValue = "true")
@Component
class BrokerPublisher(private val rabbitTemplate: RabbitTemplate) {
    fun publish(spec: PublishSpec) {
        val builder = MessageBuilder.withBody(spec.body)
            .setContentType(spec.contentType)
            .setMessageId(spec.messageId)
            .setDeliveryMode(MessageDeliveryMode.PERSISTENT)

        spec.correlationId?.let { builder.setCorrelationId(it) }
        spec.producer?.let { builder.setHeader("x-producer", it) }

        val message: Message = builder.build()
        rabbitTemplate.convertAndSend(spec.exchange, spec.routingKey, message)
    }
}

