package com.myplantdiary.contracts.events

import kotlinx.serialization.Serializable
import kotlinx.datetime.Instant

@Serializable
data class EventEnvelope<T>(
    val id: String,
    val type: String,
    val version: String,
    val occurredAt: Instant,
    val payload: T
)

