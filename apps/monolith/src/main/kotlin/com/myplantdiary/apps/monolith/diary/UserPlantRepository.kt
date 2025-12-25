package com.myplantdiary.apps.monolith.diary

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface UserPlantRepository : JpaRepository<UserPlant, UUID> {
    fun findAllByUserId(userId: UUID): List<UserPlant>
    fun findAllByUserId(userId: UUID, pageable: Pageable): Page<UserPlant>
    fun findByIdAndUserId(id: UUID, userId: UUID): UserPlant?
}
