package com.myplantdiary.apps.monolith.dictionary

import io.swagger.v3.oas.annotations.Operation
import com.myplantdiary.apps.monolith.common.PageResponse
import com.myplantdiary.apps.monolith.common.toPageResponse
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.PathVariable
import java.util.UUID

@RestController
@RequestMapping("/plants")
@Validated
class PublicPlantController(
    private val service: PlantService
) {
    @GetMapping
    @Operation(summary = "Список растений (публично, пагинация/поиск)")
    fun list(
        @RequestParam(required = false) q: String?,
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "20") size: Int,
        @RequestParam(required = false) sort: String?
    ): PageResponse<Plant> = service.searchPaged(q, page, size, sort).toPageResponse()

    @GetMapping("/{id}")
    @Operation(summary = "Получить растение по ID (публично)")
    fun get(@PathVariable id: UUID): Plant = service.get(id)

    @GetMapping("/recommendations")
    @Operation(summary = "Рекомендации по параметрам (публично)")
    fun recommend(
        @RequestParam(required = false) light: String?,
        @RequestParam(required = false) water: String?,
        @RequestParam(required = false) minTempC: Short?,
        @RequestParam(required = false) maxTempC: Short?,
        @RequestParam(required = false, defaultValue = "50") limit: Int
    ): List<Plant> = service.recommend(light, water, minTempC, maxTempC, limit)
}
