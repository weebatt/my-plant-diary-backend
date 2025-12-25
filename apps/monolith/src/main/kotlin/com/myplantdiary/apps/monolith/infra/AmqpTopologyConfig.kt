package com.myplantdiary.apps.monolith.infra

import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.DirectExchange
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.TopicExchange
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty

@Configuration
@ConditionalOnProperty(prefix = "app.messaging", name = ["enabled"], havingValue = "true")
class AmqpTopologyConfig(
    @Value("\${spring.application.name}") private val applicationName: String,
    @Value("\${app.messaging.eventsPattern:#}") private val eventsPattern: String
) {
    private val eventsExchangeName = "x.events"
    private val commandsExchangeName = "x.commands"
    private val dlxExchangeName = "x.dlx"

    private val eventsQueueName = "q.$applicationName.events"
    private val commandsQueueName = "q.$applicationName.commands"
    private val eventsDlqName = "$eventsQueueName.dlq"
    private val commandsDlqName = "$commandsQueueName.dlq"

    @Bean fun eventsExchange(): TopicExchange = TopicExchange(eventsExchangeName, true, false)
    @Bean fun commandsExchange(): DirectExchange = DirectExchange(commandsExchangeName, true, false)
    @Bean fun dlxExchange(): TopicExchange = TopicExchange(dlxExchangeName, true, false)

    @Bean fun eventsQueue(): Queue = Queue(
        eventsQueueName,
        true,
        false,
        false,
        mapOf("x-dead-letter-exchange" to dlxExchangeName)
    )

    @Bean fun commandsQueue(): Queue = Queue(
        commandsQueueName,
        true,
        false,
        false,
        mapOf("x-dead-letter-exchange" to dlxExchangeName)
    )

    @Bean fun eventsDlq(): Queue = Queue(eventsDlqName, true)
    @Bean fun commandsDlq(): Queue = Queue(commandsDlqName, true)

    @Bean
    fun bindEvents(): Binding = BindingBuilder
        .bind(eventsQueue())
        .to(eventsExchange())
        .with(eventsPattern)

    @Bean
    fun bindCommands(): Binding = BindingBuilder
        .bind(commandsQueue())
        .to(commandsExchange())
        .with(applicationName)

    @Bean
    fun bindEventsDlq(): Binding = BindingBuilder.bind(eventsDlq()).to(dlxExchange()).with("#")

    @Bean
    fun bindCommandsDlq(): Binding = BindingBuilder.bind(commandsDlq()).to(dlxExchange()).with("#")
}
