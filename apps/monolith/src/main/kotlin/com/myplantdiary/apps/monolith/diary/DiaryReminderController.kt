package com.myplantdiary.apps.monolith.diary

import com.myplantdiary.apps.monolith.common.PageResponse
import com.myplantdiary.apps.monolith.common.toPageResponse
import jakarta.validation.constraints.NotBlank
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.time.OffsetDateTime
import java.util.UUID

@RestController
@RequestMapping("/diary/reminders")
@Validated
@ConditionalOnProperty(prefix = "security.auth", name = ["enabled"], havingValue = "true")
class DiaryReminderController(
    private val diary: DiaryService
) {
    data class CreateReminderRequest(
        @field:NotBlank val kind: String,
        @field:NotBlank val dueAt: String
    )

    @GetMapping("/due")
    fun due(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestParam("before") before: String,
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "20") size: Int,
        @RequestParam(required = false) sort: String?
    ): PageResponse<Reminder> = diary.listRemindersDue(UUID.fromString(jwt.subject), OffsetDateTime.parse(before), page, size, sort).toPageResponse()

    @PostMapping("/{userPlantId}")
    fun create(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable userPlantId: UUID,
        @RequestBody req: CreateReminderRequest
    ): ResponseEntity<Reminder> {
        val userId = UUID.fromString(jwt.subject)
        val dueAt = OffsetDateTime.parse(req.dueAt)
        return ResponseEntity.ok(diary.createReminder(userId, userPlantId, req.kind, dueAt))
    }

    @PostMapping("/complete/{reminderId}")
    fun complete(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable reminderId: UUID
    ): ResponseEntity<Map<String, String>> {
        val userId = UUID.fromString(jwt.subject)
        diary.completeReminder(userId, reminderId)
        return ResponseEntity.ok(mapOf("status" to "completed"))
    }
}
