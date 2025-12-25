package com.myplantdiary.apps.monolith.dictionary

import com.myplantdiary.apps.monolith.common.BadRequestException
import com.myplantdiary.apps.monolith.common.NotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

@Service
class PlantService(
    private val repo: PlantRepository
) {
    fun get(id: UUID): Plant = repo.findById(id).orElseThrow { NotFoundException("Растение не найдено") }

    fun search(query: String?, limit: Int = 50): List<Plant> =
        if (query.isNullOrBlank()) repo.findAll().take(limit)
        else repo.findByLatinNameContainingIgnoreCaseOrCommonNameContainingIgnoreCase(query, query).take(limit)

    fun searchPaged(query: String?, page: Int, size: Int, sort: String?): org.springframework.data.domain.Page<Plant> {
        val direction = if (sort?.startsWith("-") == true) org.springframework.data.domain.Sort.Direction.DESC else org.springframework.data.domain.Sort.Direction.ASC
        val sortProp = (sort?.trim()?.trimStart('-')?.takeIf { it.isNotBlank() }) ?: "latinName"
        val pageable = org.springframework.data.domain.PageRequest.of(page.coerceAtLeast(0), size.coerceIn(1, 200), direction, sortProp)
        return if (query.isNullOrBlank()) repo.findAll(pageable)
        else repo.findByLatinNameContainingIgnoreCaseOrCommonNameContainingIgnoreCase(query, query, pageable)
    }

    @Transactional
    fun create(
        latinName: String,
        commonName: String?,
        light: String?,
        water: String?,
        minTempC: Short?,
        maxTempC: Short?,
        notes: String?
    ): Plant {
        val ln = latinName.trim()
        if (ln.isBlank()) throw BadRequestException("Latin name обязателен")
        if (repo.findByLatinNameIgnoreCase(ln).isPresent) throw BadRequestException("Растение уже существует")
        val now = OffsetDateTime.now(ZoneOffset.UTC)
        val plant = Plant(
            id = UUID.randomUUID(),
            latinName = ln,
            commonName = commonName?.trim()?.ifBlank { null },
            light = light?.trim()?.ifBlank { null },
            water = water?.trim()?.ifBlank { null },
            minTempC = minTempC,
            maxTempC = maxTempC,
            notes = notes?.trim()?.ifBlank { null },
            createdAt = now,
            updatedAt = now
        )
        return repo.save(plant)
    }

    @Transactional
    fun update(
        id: UUID,
        commonName: String?,
        light: String?,
        water: String?,
        minTempC: Short?,
        maxTempC: Short?,
        notes: String?
    ): Plant {
        val existing = get(id)
        val now = OffsetDateTime.now(ZoneOffset.UTC)
        val updated = existing.copy(
            commonName = commonName?.trim()?.ifBlank { null } ?: existing.commonName,
            light = light?.trim()?.ifBlank { null } ?: existing.light,
            water = water?.trim()?.ifBlank { null } ?: existing.water,
            minTempC = minTempC ?: existing.minTempC,
            maxTempC = maxTempC ?: existing.maxTempC,
            notes = notes?.trim()?.ifBlank { null } ?: existing.notes,
            updatedAt = now
        )
        return repo.save(updated)
    }

    @Transactional
    fun delete(id: UUID) {
        if (!repo.existsById(id)) throw NotFoundException("Растение не найдено")
        repo.deleteById(id)
    }

    fun recommend(
        light: String?,
        water: String?,
        minTempC: Short?,
        maxTempC: Short?,
        limit: Int = 50
    ): List<Plant> {
        // Черновой алгоритм: фильтр по равенству light/water (если заданы)
        // и попадание температурного диапазона растения в заданные границы
        val all = repo.findAll()
        return all.asSequence()
            .filter { p -> light.isNullOrBlank() || p.light?.equals(light, ignoreCase = true) == true }
            .filter { p -> water.isNullOrBlank() || p.water?.equals(water, ignoreCase = true) == true }
            .filter { p ->
                val okMin = minTempC?.let { mt -> p.minTempC?.let { it <= mt } ?: true } ?: true
                val okMax = maxTempC?.let { xt -> p.maxTempC?.let { it >= xt } ?: true } ?: true
                okMin && okMax
            }
            .take(limit)
            .toList()
    }
}
