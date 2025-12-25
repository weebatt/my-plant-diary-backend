package com.myplantdiary.apps.telegram.telegram

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.time.ZoneOffset

@Component
class ChatRegistry(
    private val repo: ChatRegistryRepository
) {
    private val log = LoggerFactory.getLogger(ChatRegistry::class.java)

    @Transactional
    fun put(userId: String, chatId: Long) {
        val entity = ChatRegistryEntity(userId = userId, chatId = chatId, updatedAt = OffsetDateTime.now(ZoneOffset.UTC))
        repo.save(entity)
        log.info("Связал userId={} с chatId={}", userId, chatId)
    }

    fun get(userId: String): Long? = repo.findById(userId).map { it.chatId }.orElse(null)

    fun all(): Map<String, Long> = repo.findAll().associate { it.userId to it.chatId }
}
