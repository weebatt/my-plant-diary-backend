package com.myplantdiary.apps.monolith.diary

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface CareEntryRepository : JpaRepository<CareEntry, UUID> {
    fun findAllByUserIdAndUserPlantId(userId: UUID, userPlantId: UUID): List<CareEntry>
    fun findAllByUserIdAndUserPlantId(userId: UUID, userPlantId: UUID, pageable: Pageable): Page<CareEntry>
}
