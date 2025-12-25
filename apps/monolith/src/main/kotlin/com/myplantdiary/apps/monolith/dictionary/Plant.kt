package com.myplantdiary.apps.monolith.dictionary

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "plants")
data class Plant(
    @Id val id: UUID,
    @Column(name = "latin_name", nullable = false, unique = true) val latinName: String,
    @Column(name = "common_name") val commonName: String? = null,
    @Column(name = "light") val light: String? = null,
    @Column(name = "water") val water: String? = null,
    @Column(name = "min_temp_c") val minTempC: Short? = null,
    @Column(name = "max_temp_c") val maxTempC: Short? = null,
    @Column(name = "notes") val notes: String? = null,
    @Column(name = "created_at", nullable = false) val createdAt: OffsetDateTime,
    @Column(name = "updated_at", nullable = false) val updatedAt: OffsetDateTime,
)

