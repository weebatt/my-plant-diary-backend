package com.myplantdiary.apps.monolith.diary

import com.myplantdiary.apps.monolith.common.BadRequestException
import com.myplantdiary.apps.monolith.common.NotFoundException
import com.myplantdiary.apps.monolith.messaging.MessagingPublisherService
import com.myplantdiary.apps.monolith.messaging.KotlinxJson
import events.DiaryReminderCompleted
import events.DiaryReminderCreated
import events.EventEnvelope
import kotlinx.datetime.Clock
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

@Service
class DiaryService(
    private val userPlants: UserPlantRepository,
    private val careEntries: CareEntryRepository,
    private val reminders: ReminderRepository,
    private val publisher: MessagingPublisherService
) {
    // User Plants
    fun listUserPlants(userId: UUID, page: Int, size: Int, sort: String?): org.springframework.data.domain.Page<UserPlant> {
        val direction = if (sort?.startsWith("-") == true) org.springframework.data.domain.Sort.Direction.DESC else org.springframework.data.domain.Sort.Direction.ASC
        val sortProp = (sort?.trim()?.trimStart('-')?.takeIf { it.isNotBlank() }) ?: "createdAt"
        val pageable = org.springframework.data.domain.PageRequest.of(page.coerceAtLeast(0), size.coerceIn(1, 200), direction, sortProp)
        return userPlants.findAllByUserId(userId, pageable)
    }

    @Transactional
    fun addUserPlant(userId: UUID, plantId: UUID?, nickname: String?): UserPlant {
        val now = OffsetDateTime.now(ZoneOffset.UTC)
        val entity = UserPlant(
            id = UUID.randomUUID(),
            userId = userId,
            plantId = plantId,
            nickname = nickname?.trim()?.ifBlank { null },
            createdAt = now,
            updatedAt = now
        )
        return userPlants.save(entity)
    }

    @Transactional
    fun updateUserPlant(userId: UUID, userPlantId: UUID, plantId: UUID?, nickname: String?): UserPlant {
        val existing = userPlants.findByIdAndUserId(userPlantId, userId) ?: throw NotFoundException("Растение пользователя не найдено")
        val now = OffsetDateTime.now(ZoneOffset.UTC)
        val updated = existing.copy(
            plantId = plantId ?: existing.plantId,
            nickname = nickname?.trim()?.ifBlank { null } ?: existing.nickname,
            updatedAt = now
        )
        return userPlants.save(updated)
    }

    @Transactional
    fun deleteUserPlant(userId: UUID, userPlantId: UUID) {
        val existing = userPlants.findByIdAndUserId(userPlantId, userId) ?: throw NotFoundException("Растение пользователя не найдено")
        // Опционально: можно удалить связанные записи/напоминания
        careEntries.findAllByUserIdAndUserPlantId(userId, userPlantId).forEach { careEntries.delete(it) }
        reminders.findAllByUserIdAndDueAtBefore(userId, OffsetDateTime.now(ZoneOffset.UTC).plusYears(100)).forEach {
            if (it.userPlantId == userPlantId) reminders.delete(it)
        }
        userPlants.delete(existing)
    }

    // Care entries
    fun listCareEntries(userId: UUID, userPlantId: UUID, page: Int, size: Int, sort: String?): org.springframework.data.domain.Page<CareEntry> {
        val direction = if (sort?.startsWith("-") == true) org.springframework.data.domain.Sort.Direction.DESC else org.springframework.data.domain.Sort.Direction.ASC
        val sortProp = (sort?.trim()?.trimStart('-')?.takeIf { it.isNotBlank() }) ?: "occurredAt"
        val pageable = org.springframework.data.domain.PageRequest.of(page.coerceAtLeast(0), size.coerceIn(1, 200), direction, sortProp)
        return careEntries.findAllByUserIdAndUserPlantId(userId, userPlantId, pageable)
    }

    @Transactional
    fun addCareEntry(userId: UUID, userPlantId: UUID, kind: String, notes: String?, occurredAt: OffsetDateTime?): CareEntry {
        val plant = userPlants.findByIdAndUserId(userPlantId, userId) ?: throw NotFoundException("Растение пользователя не найдено")
        val now = OffsetDateTime.now(ZoneOffset.UTC)
        val normalized = normalizeCareKind(kind).name.lowercase()
        val entry = CareEntry(
            id = UUID.randomUUID(),
            userId = userId,
            userPlantId = plant.id,
            kind = normalized,
            notes = notes?.trim()?.ifBlank { null },
            occurredAt = occurredAt ?: now,
            createdAt = now
        )
        return careEntries.save(entry)
    }

    // Reminders
    fun listRemindersDue(userId: UUID, before: OffsetDateTime, page: Int, size: Int, sort: String?): org.springframework.data.domain.Page<Reminder> {
        val direction = if (sort?.startsWith("-") == true) org.springframework.data.domain.Sort.Direction.DESC else org.springframework.data.domain.Sort.Direction.ASC
        val sortProp = (sort?.trim()?.trimStart('-')?.takeIf { it.isNotBlank() }) ?: "dueAt"
        val pageable = org.springframework.data.domain.PageRequest.of(page.coerceAtLeast(0), size.coerceIn(1, 200), direction, sortProp)
        return reminders.findAllByUserIdAndDueAtBefore(userId, before, pageable)
    }

    @Transactional
    fun createReminder(userId: UUID, userPlantId: UUID, kind: String, dueAt: OffsetDateTime): Reminder {
        val plant = userPlants.findByIdAndUserId(userPlantId, userId) ?: throw NotFoundException("Растение пользователя не найдено")
        val now = OffsetDateTime.now(ZoneOffset.UTC)
        val normalized = normalizeReminderKind(kind).name.lowercase()
        val reminder = Reminder(
            id = UUID.randomUUID(),
            userId = userId,
            userPlantId = plant.id,
            kind = normalized,
            dueAt = dueAt,
            createdAt = now,
            updatedAt = now
        )
        val saved = reminders.save(reminder)
        publishReminderCreated(saved)
        return saved
    }

    @Transactional
    fun completeReminder(userId: UUID, reminderId: UUID) {
        val reminder = reminders.findByIdAndUserId(reminderId, userId) ?: throw NotFoundException("Напоминание не найдено")
        reminders.delete(reminder)
        publishReminderCompleted(reminder)
    }

    private fun publishReminderCreated(r: Reminder) {
        val env = EventEnvelope(
            id = UUID.randomUUID().toString(),
            type = "diary.reminder.created",
            version = "1",
            occurredAt = Clock.System.now(),
            payload = DiaryReminderCreated(
                reminderId = r.id.toString(),
                userId = r.userId.toString(),
                userPlantId = r.userPlantId.toString(),
                dueAtEpochMs = r.dueAt.toInstant().toEpochMilli(),
                kind = r.kind
            )
        )
        publisher.publishEvent(env, DiaryReminderCreated.serializer())
    }

    private fun publishReminderCompleted(r: Reminder) {
        val env = EventEnvelope(
            id = UUID.randomUUID().toString(),
            type = "diary.reminder.completed",
            version = "1",
            occurredAt = Clock.System.now(),
            payload = DiaryReminderCompleted(
                reminderId = r.id.toString(),
                userId = r.userId.toString(),
                userPlantId = r.userPlantId.toString(),
                completedAt = System.currentTimeMillis(),
                kind = r.kind
            )
        )
        publisher.publishEvent(env, DiaryReminderCompleted.serializer())
    }
}
