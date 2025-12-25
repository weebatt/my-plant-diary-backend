package com.myplantdiary.apps.monolith.diary

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "user_plants")
data class UserPlant(
    @Id val id: UUID,
    @Column(name = "user_id", nullable = false) val userId: UUID,
    @Column(name = "plant_id") val plantId: UUID?,
    @Column(name = "nickname") val nickname: String?,
    @Column(name = "created_at", nullable = false) val createdAt: OffsetDateTime,
    @Column(name = "updated_at", nullable = false) val updatedAt: OffsetDateTime,
)

