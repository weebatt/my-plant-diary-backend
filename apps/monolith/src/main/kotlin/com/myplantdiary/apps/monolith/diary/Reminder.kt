package com.myplantdiary.apps.monolith.diary

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "reminders")
data class Reminder(
    @Id val id: UUID,
    @Column(name = "user_id", nullable = false) val userId: UUID,
    @Column(name = "user_plant_id", nullable = false) val userPlantId: UUID,
    @Column(name = "kind", nullable = false) val kind: String,
    @Column(name = "due_at", nullable = false) val dueAt: OffsetDateTime,
    @Column(name = "created_at", nullable = false) val createdAt: OffsetDateTime,
    @Column(name = "updated_at", nullable = false) val updatedAt: OffsetDateTime,
)

