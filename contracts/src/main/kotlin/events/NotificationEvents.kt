package com.myplantdiary.contracts.events

import kotlinx.serialization.Serializable

@Serializable
data class ReminderDue(
    val reminderId: String,
    val userId: String,
    val dueAtEpochMs: Long
)

@Serializable
data class ChannelSend(
    val channel: String, // telegram|webpush|avito
    val correlationId: String,
    val userId: String,
    val content: String
)

@Serializable
data class NotificationSent(
    val correlationId: String,
    val channel: String
)

@Serializable
data class NotificationFailed(
    val correlationId: String,
    val channel: String,
    val reason: String
)
