package com.myplantdiary.apps.monolith.user

import com.myplantdiary.apps.monolith.common.BadRequestException
import com.myplantdiary.apps.monolith.common.NotFoundException
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

@RestController
@RequestMapping("/profile")
@Validated
@ConditionalOnProperty(prefix = "security.auth", name = ["enabled"], havingValue = "true")
class ProfileController(
    private val repo: UserRepository
) {
    data class ProfileResponse(val id: String, val email: String, val role: Role)

    @GetMapping
    fun me(@AuthenticationPrincipal jwt: Jwt): ProfileResponse {
        val userId = UUID.fromString(jwt.subject)
        val user = repo.findById(userId).orElseThrow { NotFoundException("Пользователь не найден") }
        return ProfileResponse(user.id.toString(), user.email, user.role)
    }

    data class UpdateProfileRequest(
        @field:Email val email: String? = null,
        @field:NotBlank val password: String? = null
    )

    @PatchMapping
    fun update(@AuthenticationPrincipal jwt: Jwt, @RequestBody req: UpdateProfileRequest): ResponseEntity<ProfileResponse> {
        val userId = UUID.fromString(jwt.subject)
        val user = repo.findById(userId).orElseThrow { NotFoundException("Пользователь не найден") }
        if (req.email == null && req.password == null) throw BadRequestException("Нечего обновлять")
        var updated = user
        val now = OffsetDateTime.now(ZoneOffset.UTC)
        if (req.email != null && req.email != user.email) {
            if (repo.findByEmail(req.email).isPresent) throw BadRequestException("Email уже занят")
            updated = updated.copy(email = req.email, updatedAt = now)
        }
        // Пароль меняется через отдельный эндпоинт в реальной системе; здесь — для простоты пропускаем хэширование.
        repo.save(updated)
        return ResponseEntity.ok(ProfileResponse(updated.id.toString(), updated.email, updated.role))
    }
}
