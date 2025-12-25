package com.myplantdiary.apps.monolith.user

import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID
import org.springframework.security.crypto.bcrypt.BCrypt

@Component
@ConditionalOnProperty(prefix = "security.auth", name = ["enabled"], havingValue = "true")
class AdminInitRunner(
    private val repo: UserRepository
) : ApplicationRunner {
    private val log = LoggerFactory.getLogger(AdminInitRunner::class.java)

    override fun run(args: ApplicationArguments) {
        val adminEmail = System.getenv("SEED_ADMIN_EMAIL") ?: "admin@example.com"
        val adminPassword = System.getenv("SEED_ADMIN_PASSWORD") ?: "admin123"
        if (repo.findByEmail(adminEmail).isPresent) return
        val now = OffsetDateTime.now(ZoneOffset.UTC)
        val user = User(
            id = UUID.randomUUID(),
            email = adminEmail,
            passwordHash = BCrypt.hashpw(adminPassword, BCrypt.gensalt(10)),
            role = Role.ADMIN,
            createdAt = now,
            updatedAt = now,
        )
        repo.save(user)
        log.info("Создан admin пользователь: {} (пароль по умолчанию)", adminEmail)
    }
}

