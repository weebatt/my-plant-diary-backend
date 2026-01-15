package com.myplantdiary.apps.monolith.telegram

import com.myplantdiary.apps.monolith.security.SecurityProps
import io.swagger.v3.oas.annotations.Operation
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

@RestController
@RequestMapping("/telegram/link")
@Validated
@ConditionalOnProperty(prefix = "security.auth", name = ["enabled"], havingValue = "true")
class TelegramLinkController(
    private val props: SecurityProps,
    private val linkTokens: LinkTokenRepository,
) {
    data class StartResponse(val token: String)

    @PostMapping("/start")
    @Operation(summary = "Сгенерировать bind-токен для привязки Telegram через /start <token>")
    fun start(@AuthenticationPrincipal jwt: Jwt): ResponseEntity<StartResponse> {
        val userId = java.util.UUID.fromString(jwt.subject)
        val now = OffsetDateTime.now(ZoneOffset.UTC)
        val expAt = now.plusMinutes(30)
        val token = issueShortToken()
        linkTokens.save(LinkToken(token = token, userId = userId, expiresAt = expAt, createdAt = now, used = false))
        return ResponseEntity.ok(StartResponse(token))
    }

    // Вариант B: короткий одноразовый токен (без JWT). 16 байт = 22 симв base64url.
    private fun issueShortToken(): String {
        val bytes = java.security.SecureRandom().generateSeed(16)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    private fun hmacBindToken(userId: String, exp: Long): String {
        val header = base64Url("{" + "\"alg\":\"HS256\",\"typ\":\"JWT\"}")
        val payload = base64Url("{" +
            "\"typ\":\"bind\"," +
            "\"sub\":\"$userId\"," +
            "\"exp\":$exp}")
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
