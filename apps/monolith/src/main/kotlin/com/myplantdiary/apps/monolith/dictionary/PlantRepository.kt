package com.myplantdiary.apps.monolith.dictionary

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional
import java.util.UUID

interface PlantRepository : JpaRepository<Plant, UUID> {
    fun findByLatinNameIgnoreCase(latinName: String): Optional<Plant>
    fun findByLatinNameContainingIgnoreCaseOrCommonNameContainingIgnoreCase(
        latinName: String,
        commonName: String
    ): List<Plant>

    fun findByLatinNameContainingIgnoreCaseOrCommonNameContainingIgnoreCase(
        latinName: String,
        commonName: String,
        pageable: Pageable
    ): Page<Plant>
}
