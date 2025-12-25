package com.myplantdiary.apps.monolith.messaging

import org.springframework.amqp.rabbit.annotation.EnableRabbit
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration

@Configuration
@EnableRabbit
@ConditionalOnProperty(prefix = "app.messaging", name = ["enabled"], havingValue = "true")
class MessagingConfig

