package com.myplantdiary.apps.monolith.messaging

import com.myplantdiary.apps.monolith.infra.BrokerPublisher
import com.myplantdiary.apps.monolith.infra.PublishSpec
import events.EventEnvelope
import kotlinx.serialization.KSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@Service
@ConditionalOnProperty(prefix = "app.messaging", name = ["enabled"], havingValue = "true")
class MessagingPublisherService(
    private val brokerPublisher: BrokerPublisher,
    @Value("\${spring.application.name}") private val applicationName: String,
) {
    private val json = KotlinxJson.json

    fun <T> publishEvent(envelope: EventEnvelope<T>, payloadSerializer: KSerializer<T>) {
        val routingKey = "${envelope.type}.v${envelope.version}"
        val body = json.encodeToString(EventEnvelope.serializer(payloadSerializer), envelope)
            .toByteArray(Charsets.UTF_8)
        brokerPublisher.publish(
            PublishSpec(
                exchange = "x.events",
                routingKey = routingKey,
                body = body,
                messageId = envelope.id,
                producer = applicationName,
            )
        )
    }
}

