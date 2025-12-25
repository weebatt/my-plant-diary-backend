package com.myplantdiary.apps.monolith.user

import com.myplantdiary.apps.monolith.common.BadRequestException
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import jakarta.validation.constraints.NotBlank
import java.util.UUID

@RestController
@RequestMapping("/profile/password")
@Validated
@ConditionalOnProperty(prefix = "security.auth", name = ["enabled"], havingValue = "true")
class PasswordController(
    private val users: UserService
) {
    data class ChangePasswordRequest(
        @field:NotBlank val currentPassword: String,
        @field:NotBlank val newPassword: String
    )

    @PostMapping
    fun changePassword(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestBody req: ChangePasswordRequest
    ): ResponseEntity<Map<String, String>> {
        if (req.currentPassword == req.newPassword) throw BadRequestException("Новый пароль совпадает со старым")
        val userId = UUID.fromString(jwt.subject)
        users.changePassword(userId, req.currentPassword, req.newPassword)
        return ResponseEntity.ok(mapOf("status" to "ok"))
    }
}

