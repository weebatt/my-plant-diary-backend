package com.myplantdiary.apps.telegram.infra

import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.amqp.support.converter.SimpleMessageConverter
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.amqp.core.AcknowledgeMode

@Configuration
class BrokerConsumerConfig(
    @Value("\${app.messaging.prefetch:20}") private val prefetch: Int
) {
    @Bean
    fun messageConverter(): MessageConverter = SimpleMessageConverter()

    @Bean(name = ["manualAckListenerContainerFactory"])
    fun manualAckListenerContainerFactory(
        connectionFactory: ConnectionFactory,
        messageConverter: MessageConverter
    ): SimpleRabbitListenerContainerFactory {
        val factory = SimpleRabbitListenerContainerFactory()
        factory.setConnectionFactory(connectionFactory)
        factory.setMessageConverter(messageConverter)
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL)
        factory.setPrefetchCount(prefetch)
        factory.setDefaultRequeueRejected(false)
        factory.setMissingQueuesFatal(false)
        return factory
    }
}

