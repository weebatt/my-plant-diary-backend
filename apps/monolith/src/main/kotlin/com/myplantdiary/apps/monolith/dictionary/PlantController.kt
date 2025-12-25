package com.myplantdiary.apps.monolith.dictionary

import com.myplantdiary.apps.monolith.user.Role
import jakarta.validation.constraints.NotBlank
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/admin/plants")
@Validated
@ConditionalOnProperty(prefix = "security.auth", name = ["enabled"], havingValue = "true")
class PlantController(
    private val service: PlantService
) {
    data class CreatePlantRequest(
        @field:NotBlank val latinName: String,
        val commonName: String? = null,
        val light: String? = null,
        val water: String? = null,
        val minTempC: Short? = null,
        val maxTempC: Short? = null,
        val notes: String? = null
    )

    data class UpdatePlantRequest(
        val commonName: String? = null,
        val light: String? = null,
        val water: String? = null,
        val minTempC: Short? = null,
        val maxTempC: Short? = null,
        val notes: String? = null
    )

    @GetMapping
    @PreAuthorize("hasAuthority('SCOPE_mpd') or hasAuthority('ROLE_ADMIN') or hasAuthority('ADMIN')")
    fun list(@RequestParam(required = false) q: String?): List<Plant> = service.search(q)

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_mpd') or hasAuthority('ROLE_ADMIN') or hasAuthority('ADMIN')")
    fun get(@PathVariable id: UUID): Plant = service.get(id)

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ADMIN')")
    fun create(@RequestBody req: CreatePlantRequest): ResponseEntity<Plant> =
        ResponseEntity.ok(
            service.create(req.latinName, req.commonName, req.light, req.water, req.minTempC, req.maxTempC, req.notes)
        )

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ADMIN')")
    fun update(@PathVariable id: UUID, @RequestBody req: UpdatePlantRequest): ResponseEntity<Plant> =
        ResponseEntity.ok(
            service.update(id, req.commonName, req.light, req.water, req.minTempC, req.maxTempC, req.notes)
        )

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ADMIN')")
    fun delete(@PathVariable id: UUID): ResponseEntity<Map<String, String>> {
        service.delete(id)
        return ResponseEntity.ok(mapOf("status" to "deleted"))
    }
}

