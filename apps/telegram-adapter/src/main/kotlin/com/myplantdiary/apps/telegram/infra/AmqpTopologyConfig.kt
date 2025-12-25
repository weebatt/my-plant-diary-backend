package com.myplantdiary.apps.telegram.infra

import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.DirectExchange
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.TopicExchange
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AmqpTopologyConfig(
    @Value("\${spring.application.name}") private val applicationName: String
) {
    private val eventsExchangeName = "x.events"
    private val commandsExchangeName = "x.commands"
    private val dlxExchangeName = "x.dlx"

    private val commandsQueueName = "q.$applicationName.commands"
    private val commandsDlqName = "$commandsQueueName.dlq"

    @Bean fun eventsExchange(): TopicExchange = TopicExchange(eventsExchangeName, true, false)
    @Bean fun commandsExchange(): DirectExchange = DirectExchange(commandsExchangeName, true, false)
    @Bean fun dlxExchange(): TopicExchange = TopicExchange(dlxExchangeName, true, false)

    @Bean fun commandsQueue(): Queue = Queue(
        commandsQueueName,
        true,
        false,
        false,
        mapOf("x-dead-letter-exchange" to dlxExchangeName)
    )

    @Bean fun commandsDlq(): Queue = Queue(commandsDlqName, true)

    @Bean
    fun bindCommands(): Binding = BindingBuilder
        .bind(commandsQueue())
        .to(commandsExchange())
        .with(applicationName)

    @Bean
    fun bindCommandsDlq(): Binding = BindingBuilder.bind(commandsDlq()).to(dlxExchange()).with("#")
}

