package com.myplantdiary.apps.monolith.diary

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "care_entries")
data class CareEntry(
    @Id val id: UUID,
    @Column(name = "user_id", nullable = false) val userId: UUID,
    @Column(name = "user_plant_id", nullable = false) val userPlantId: UUID,
    @Column(name = "kind", nullable = false) val kind: String,
    @Column(name = "notes") val notes: String?,
    @Column(name = "occurred_at", nullable = false) val occurredAt: OffsetDateTime,
    @Column(name = "created_at", nullable = false) val createdAt: OffsetDateTime,
)

