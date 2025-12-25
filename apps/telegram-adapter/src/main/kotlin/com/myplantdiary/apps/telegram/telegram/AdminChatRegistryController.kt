package com.myplantdiary.apps.telegram.telegram

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/admin/chat-registry")
class AdminChatRegistryController(
    private val registry: ChatRegistry
) {
    data class UpsertRequest(val userId: String, val chatId: Long)

    @GetMapping
    fun list(): Map<String, Long> = registry.all()

    @PostMapping
    fun upsert(@RequestBody req: UpsertRequest): ResponseEntity<Map<String, String>> {
        registry.put(req.userId, req.chatId)
        return ResponseEntity.ok(mapOf("status" to "ok"))
    }
}

