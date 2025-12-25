package com.myplantdiary.apps.monolith.diary

import com.myplantdiary.apps.monolith.common.BadRequestException
import com.myplantdiary.apps.monolith.common.PageResponse
import com.myplantdiary.apps.monolith.common.toPageResponse
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/diary/plants")
@Validated
@ConditionalOnProperty(prefix = "security.auth", name = ["enabled"], havingValue = "true")
class DiaryUserPlantsController(
    private val diary: DiaryService
) {
    data class CreateRequest(
        val plantId: UUID? = null,
        val nickname: String? = null
    )
    data class UpdateRequest(
        val plantId: UUID? = null,
        val nickname: String? = null
    )

    @GetMapping
    fun list(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "20") size: Int,
        @RequestParam(required = false) sort: String?
    ): PageResponse<UserPlant> = diary.listUserPlants(UUID.fromString(jwt.subject), page, size, sort).toPageResponse()

    @PostMapping
    fun create(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestBody req: CreateRequest
    ): ResponseEntity<UserPlant> {
        val userId = UUID.fromString(jwt.subject)
        val created = diary.addUserPlant(userId, req.plantId, req.nickname)
        return ResponseEntity.ok(created)
    }

    @PatchMapping("/{id}")
    fun update(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable id: UUID,
        @RequestBody req: UpdateRequest
    ): ResponseEntity<UserPlant> {
        val userId = UUID.fromString(jwt.subject)
        if (req.plantId == null && (req.nickname == null)) throw BadRequestException("Нечего обновлять")
        val updated = diary.updateUserPlant(userId, id, req.plantId, req.nickname)
        return ResponseEntity.ok(updated)
    }

    @DeleteMapping("/{id}")
    fun delete(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable id: UUID
    ): ResponseEntity<Map<String, String>> {
        val userId = UUID.fromString(jwt.subject)
        diary.deleteUserPlant(userId, id)
        return ResponseEntity.ok(mapOf("status" to "deleted"))
    }
}
