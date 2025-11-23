package events

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class EventEnvelope<T>(
    val id: String,
    val type: String,
    val version: String,
    val occurredAt: Instant,
    val payload: T
)
