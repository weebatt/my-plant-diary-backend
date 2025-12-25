package com.myplantdiary.apps.monolith.auth

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/me")
@ConditionalOnProperty(prefix = "security.auth", name = ["enabled"], havingValue = "true")
class MeController {
    @GetMapping
    fun me(@AuthenticationPrincipal jwt: Jwt): Map<String, Any?> = mapOf(
        "sub" to jwt.subject,
        "email" to jwt.claims["email"],
        "role" to jwt.claims["role"],
        "iss" to jwt.issuer?.toString(),
        "aud" to jwt.audience
    )
}

