package com.myplantdiary.apps.monolith.diary

enum class CareKind { WATER, FERTILIZE, MIST, REPOT }
enum class ReminderKind { WATER, FERTILIZE, MIST, REPOT }

fun normalizeCareKind(value: String): CareKind = try {
    CareKind.valueOf(value.trim().uppercase())
} catch (e: Exception) {
    throw IllegalArgumentException("Недопустимый вид ухода: $value")
}

fun normalizeReminderKind(value: String): ReminderKind = try {
    ReminderKind.valueOf(value.trim().uppercase())
} catch (e: Exception) {
    throw IllegalArgumentException("Недопустимый вид напоминания: $value")
}

