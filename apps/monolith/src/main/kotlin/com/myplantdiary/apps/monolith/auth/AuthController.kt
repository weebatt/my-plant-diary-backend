package com.myplantdiary.apps.monolith.auth

import io.swagger.v3.oas.annotations.Operation
import com.myplantdiary.apps.monolith.security.SecurityProps
import com.myplantdiary.apps.monolith.user.Role
import com.myplantdiary.apps.monolith.user.User
import com.myplantdiary.apps.monolith.user.UserService
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

@RestController
@RequestMapping("/auth")
@Validated
class AuthController(
    private val users: UserService,
    private val props: SecurityProps
) {
    data class RegisterRequest(
        @field:Email val email: String,
        @field:NotBlank val password: String
    )
    data class RegisterResponse(val id: String, val email: String)

    @PostMapping("/register")
    @Operation(summary = "Регистрация пользователя (всегда доступно)")
    fun register(@Valid @RequestBody req: RegisterRequest): ResponseEntity<RegisterResponse> {
        val u = users.register(req.email, req.password, Role.USER)
        return ResponseEntity.ok(RegisterResponse(u.id.toString(), u.email))
    }

    data class LoginRequest(
        @field:Email val email: String,
        @field:NotBlank val password: String
    )
    data class LoginResponse(val token: String)

    @PostMapping("/login")
    @Operation(summary = "Логин (JWT): возвращает токен при включённой авторизации")
    @ConditionalOnProperty(prefix = "security.auth", name = ["enabled"], havingValue = "true")
    fun login(@Valid @RequestBody req: LoginRequest): ResponseEntity<LoginResponse> {
        val user = users.verify(req.email, req.password) ?: return ResponseEntity.status(401).build()
        val token = hmacJwt(user)
        return ResponseEntity.ok(LoginResponse(token))
    }

    private fun hmacJwt(user: User): String {
        val issuedAt = Instant.now().epochSecond
        val header = base64Url("{" +
                "\"alg\":\"HS256\",\"typ\":\"JWT\"}")
        val payload = base64Url("{" +
                "\"iss\":\"${props.jwt.issuer}\"," +
                "\"aud\":\"${props.jwt.audience}\"," +
                "\"sub\":\"${user.id}\"," +
                "\"email\":\"${user.email}\"," +
                "\"role\":\"${user.role}\"," +
                "\"iat\":$issuedAt}")
        val signingInput = "$header.$payload"
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(props.jwt.secret.toByteArray(), "HmacSHA256"))
        val sig = mac.doFinal(signingInput.toByteArray())
        val signature = Base64.getUrlEncoder().withoutPadding().encodeToString(sig)
        return "$signingInput.$signature"
    }

    private fun base64Url(s: String): String =
        Base64.getUrlEncoder().withoutPadding().encodeToString(s.toByteArray())
}
