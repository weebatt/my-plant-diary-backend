package com.myplantdiary.apps.telegram.telegram

import com.myplantdiary.apps.telegram.security.SecurityProps
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

@Component
class BindTokenValidator(
    private val props: SecurityProps,
    private val jdbc: JdbcTemplate,
) {
    data class Result(val ok: Boolean, val userId: String?)

    fun validate(token: String): Result {
        // Вариант B: короткий одноразовый токен из таблицы link_tokens
        val rows: List<String> = jdbc.query(
            "SELECT user_id FROM link_tokens WHERE token = ? AND used = false AND expires_at > now()",
            arrayOf<Any>(token)
        ) { rs, _ -> rs.getString(1) }
        val dbUser = rows.firstOrNull()
        if (dbUser != null) {
            // помечаем как использованный (best-effort)
            runCatching { jdbc.update("UPDATE link_tokens SET used = true WHERE token = ?", token) }
            return Result(true, dbUser)
        }

        // very small JWT-like: header.payload.signature (base64url). payload has {"typ":"bind","sub":"...","exp":...}
        val parts = token.split('.')
        if (parts.size != 3) return Result(false, null)
        val (header, payload, signature) = parts
        val signingInput = "$header.$payload"
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(props.jwt.secret.toByteArray(), "HmacSHA256"))
        val expected = Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(signingInput.toByteArray()))
        if (expected != signature) return Result(false, null)

        val json = String(Base64.getUrlDecoder().decode(payload))
        // naive JSON matchers using raw strings
        val hasBindType = Regex("\"typ\"\\s*:\\s*\"bind\"").containsMatchIn(json)
        if (!hasBindType) return Result(false, null)
        val sub = Regex("\"sub\"\\s*:\\s*\"([^\"]+)\"").find(json)?.groupValues?.get(1) ?: return Result(false, null)
        val exp = Regex("\"exp\"\\s*:\\s*(\\d+)").find(json)?.groupValues?.get(1)?.toLongOrNull() ?: return Result(false, null)
        val now = System.currentTimeMillis() / 1000
        if (now > exp) return Result(false, null)
        return Result(true, sub)
    }
}
