package com.myplantdiary.apps.monolith.user

import org.springframework.security.crypto.bcrypt.BCrypt
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID
import com.myplantdiary.apps.monolith.common.ConflictException

@Service
class UserService(
    private val repo: UserRepository
) {
    @Transactional
    fun register(email: String, password: String, role: Role = Role.USER): User {
        require(email.isNotBlank()) { "email пустой" }
        require(password.length >= 6) { "слишком короткий пароль" }
        val normalized = email.trim().lowercase()
        if (repo.findByEmail(normalized).isPresent) throw ConflictException("Email уже зарегистрирован")
        val now = OffsetDateTime.now(ZoneOffset.UTC)
        val user = User(
            id = UUID.randomUUID(),
            email = normalized,
            passwordHash = BCrypt.hashpw(password, BCrypt.gensalt(10)),
            role = role,
            createdAt = now,
            updatedAt = now
        )
        return repo.save(user)
    }

    fun verify(email: String, password: String): User? {
        val user = repo.findByEmail(email.trim().lowercase()).orElse(null) ?: return null
        return if (BCrypt.checkpw(password, user.passwordHash)) user else null
    }

    @Transactional
    fun changePassword(userId: UUID, currentPassword: String, newPassword: String) {
        require(newPassword.length >= 6) { "слишком короткий пароль" }
        val user = repo.findById(userId).orElseThrow { IllegalArgumentException("Пользователь не найден") }
        if (!BCrypt.checkpw(currentPassword, user.passwordHash)) error("текущий пароль неверный")
        val now = OffsetDateTime.now(ZoneOffset.UTC)
        val updated = user.copy(passwordHash = BCrypt.hashpw(newPassword, BCrypt.gensalt(10)), updatedAt = now)
        repo.save(updated)
    }
}
