package com.myplantdiary.apps.monolith.dictionary

import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.time.OffsetDateTime
import java.time.ZoneOffset

@Component
@ConditionalOnProperty(prefix = "seed.plants", name = ["enabled"], havingValue = "true")
class PlantsSeedRunner(
    private val service: PlantService
) : ApplicationRunner {
    private val log = LoggerFactory.getLogger(PlantsSeedRunner::class.java)

    override fun run(args: ApplicationArguments) {
        val samples = listOf(
            Triple("Ficus elastica", "Резиновое дерево", "bright") to ("moderate" to "Неприхотливое, любит рассеянный свет"),
            Triple("Monstera deliciosa", "Монстера", "medium") to ("moderate" to "Нуждается в опоре, тёплое место"),
            Triple("Spathiphyllum wallisii", "Спатифиллум", "low") to ("moderate" to "Поддерживать влажность"),
        )
        var created = 0
        samples.forEach { (triple, pair) ->
            val (latin, common, light) = triple
            val (water, notes) = pair
            try {
                service.create(
                    latinName = latin,
                    commonName = common,
                    light = light,
                    water = water,
                    minTempC = 15,
                    maxTempC = 28,
                    notes = notes
                )
                created++
            } catch (e: Exception) {
                // вероятно, уже посеяно — пропускаем
            }
        }
        if (created > 0) log.info("Посеяно растений в словарь: {}", created)
    }
}

