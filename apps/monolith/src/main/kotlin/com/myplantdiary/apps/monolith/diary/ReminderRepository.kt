package com.myplantdiary.apps.monolith.diary

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.time.OffsetDateTime
import java.util.UUID

interface ReminderRepository : JpaRepository<Reminder, UUID> {
    fun findAllByUserIdAndDueAtBefore(userId: UUID, before: OffsetDateTime): List<Reminder>
    fun findByIdAndUserId(id: UUID, userId: UUID): Reminder?
    fun findAllByUserIdAndDueAtBefore(userId: UUID, before: OffsetDateTime, pageable: Pageable): Page<Reminder>
    fun findAllByUserIdAndDueAtBetween(userId: UUID, start: OffsetDateTime, end: OffsetDateTime, pageable: Pageable): Page<Reminder>
}
