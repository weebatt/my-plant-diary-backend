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
import java.time.ZoneOffset
import java.util.UUID

@RestController
@RequestMapping("/diary/care")
@Validated
@ConditionalOnProperty(prefix = "security.auth", name = ["enabled"], havingValue = "true")
class DiaryCareController(
    private val diary: DiaryService
) {
    data class AddCareRequest(
        @field:NotBlank val kind: String,
        val notes: String? = null,
        val occurredAt: String? = null
    )

    @GetMapping("/{userPlantId}")
    fun list(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable userPlantId: UUID,
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "20") size: Int,
        @RequestParam(required = false) sort: String?
    ): PageResponse<CareEntry> = diary.listCareEntries(UUID.fromString(jwt.subject), userPlantId, page, size, sort).toPageResponse()

    @PostMapping("/{userPlantId}")
    fun add(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable userPlantId: UUID,
        @RequestBody req: AddCareRequest
    ): ResponseEntity<CareEntry> {
        val userId = UUID.fromString(jwt.subject)
        val occurred = req.occurredAt?.let { OffsetDateTime.parse(it) }
        val created = diary.addCareEntry(userId, userPlantId, req.kind, req.notes, occurred)
        return ResponseEntity.ok(created)
    }
}
