package com.myplantdiary.apps.monolith.telegram

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "link_tokens")
data class LinkToken(
    @Id val token: String,
    @Column(name = "user_id", nullable = false) val userId: UUID,
    @Column(name = "expires_at", nullable = false) val expiresAt: OffsetDateTime,
    @Column(name = "created_at", nullable = false) val createdAt: OffsetDateTime,
    @Column(name = "used", nullable = false) val used: Boolean = false,
)

