package com.myplantdiary.apps.telegram.telegram

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.OffsetDateTime

@Entity
@Table(name = "tg_chat_registry")
data class ChatRegistryEntity(
    @Id
    @Column(name = "user_id", nullable = false)
    val userId: String,
    @Column(name = "chat_id", nullable = false)
    val chatId: Long,
    @Column(name = "updated_at", nullable = false)
    val updatedAt: OffsetDateTime
)

