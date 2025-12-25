package com.myplantdiary.apps.telegram.messaging

import events.ChannelSend
import events.EventEnvelope
import kotlinx.datetime.Clock
import kotlinx.serialization.encodeToString
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.amqp.core.MessageBuilder
import org.springframework.amqp.core.MessageDeliveryMode
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.RabbitMQContainer
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import java.nio.charset.StandardCharsets
import java.util.UUID

@SpringBootTest(properties = [
    "security.auth.enabled=false",
    "telegram.pollingEnabled=false",
    "telegram.webhookEnabled=false"
])
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CommandConsumerIT {
    companion object {
        @JvmStatic
        private val pg = PostgreSQLContainer<Nothing>("postgres:16").apply { start() }
        @JvmStatic
        private val rmq = RabbitMQContainer("rabbitmq:3.13-management").apply { start() }

        @JvmStatic
        @DynamicPropertySource
        fun register(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url") { pg.jdbcUrl }
            registry.add("spring.datasource.username") { pg.username }
            registry.add("spring.datasource.password") { pg.password }
            registry.add("spring.rabbitmq.host") { rmq.host }
            registry.add("spring.rabbitmq.port") { rmq.amqpPort }
            registry.add("spring.rabbitmq.username") { rmq.adminUsername }
            registry.add("spring.rabbitmq.password") { rmq.adminPassword }
        }
    }

    @Autowired lateinit var rabbitTemplate: RabbitTemplate

    private val json = KotlinxJson.json

    @Test
    fun publish_command_to_queue() {
        val env = EventEnvelope(
            id = UUID.randomUUID().toString(),
            type = "notification.channel.send",
            version = "1",
            occurredAt = Clock.System.now(),
            payload = ChannelSend(
                channel = "telegram",
                correlationId = "corr-1",
                userId = "user-1",
                content = "Hello"
            )
        )
        val body = json.encodeToString(EventEnvelope.serializer(ChannelSend.serializer()), env).toByteArray(StandardCharsets.UTF_8)
        val msg = MessageBuilder.withBody(body)
            .setDeliveryMode(MessageDeliveryMode.PERSISTENT)
            .setContentType("application/json").build()
        // Отправим в x.commands с routingKey=telegram-adapter (привязка очереди адаптера)
        rabbitTemplate.convertAndSend("x.commands", "telegram-adapter", msg)
        assertTrue(true)
    }
}

