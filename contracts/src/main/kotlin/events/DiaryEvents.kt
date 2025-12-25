package events

import kotlinx.serialization.Serializable

@Serializable
data class DiaryReminderCreated(
    val reminderId: String,
    val userId: String,
    val userPlantId: String,
    val dueAtEpochMs: Long,
    val kind: String
)

@Serializable
data class DiaryReminderCompleted(
    val reminderId: String,
    val userId: String,
    val userPlantId: String,
    val completedAt: Long,
    val kind: String
)

