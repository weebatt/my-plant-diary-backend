package com.myplantdiary.apps.monolith

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.containers.PostgreSQLContainer
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = [
        "security.auth.enabled=true",
        "security.auth.jwt.secret=test-secret",
        "security.auth.jwt.issuer=http://localhost",
        "security.auth.jwt.audience=mpd",
        "APP_MESSAGING_ENABLED=false",
        "SEED_ADMIN_EMAIL=admin@example.com",
        "SEED_ADMIN_PASSWORD=admin123"
    ])
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MonolithE2eTest {

    companion object {
        @JvmStatic
        private val pg = PostgreSQLContainer<Nothing>("postgres:16").apply {
            withDatabaseName("mpd")
            withUsername("mpd")
            withPassword("mpd")
            start()
        }

        @JvmStatic
        @DynamicPropertySource
        fun register(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url") { pg.jdbcUrl }
            registry.add("spring.datasource.username") { pg.username }
            registry.add("spring.datasource.password") { pg.password }
        }
    }

    @LocalServerPort
    private var port: Int = 0

    @Autowired
    private lateinit var rest: TestRestTemplate

    private fun url(path: String) = "http://localhost:$port$path"

    @Test
    fun end_to_end_smoke() {
        // 1) Логин админа
        val loginResp = rest.postForEntity(url("/auth/login"), mapOf(
            "email" to "admin@example.com",
            "password" to "admin123"
        ), Map::class.java)
        assertEquals(200, loginResp.statusCode.value())
        val token = (loginResp.body?.get("token") as String?)
        assertNotNull(token)

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(token!!)

        // 2) Admin: создать растение
        val createPlant = rest.exchange(url("/admin/plants"), HttpMethod.POST, HttpEntity(mapOf(
            "latinName" to "Testus plantus ${UUID.randomUUID()}",
            "commonName" to "Тестовое",
            "light" to "bright",
            "water" to "moderate"
        ), headers), Map::class.java)
        assertEquals(200, createPlant.statusCode.value())

        // 3) Публичный список растений
        val plantsResp = rest.getForEntity(url("/plants?page=0&size=1"), Map::class.java)
        assertEquals(200, plantsResp.statusCode.value())

        // 4) Дневник: добавить моё растение
        val myPlantResp = rest.exchange(url("/diary/plants"), HttpMethod.POST, HttpEntity(mapOf(
            "nickname" to "Моё"
        ), headers), Map::class.java)
        assertEquals(200, myPlantResp.statusCode.value())
        val userPlantId = (myPlantResp.body?.get("id") as String?)
        assertNotNull(userPlantId)

        // 5) Создать напоминание
        val remResp = rest.exchange(url("/diary/reminders/$userPlantId"), HttpMethod.POST, HttpEntity(mapOf(
            "kind" to "water",
            "dueAt" to "2030-01-01T00:00:00Z"
        ), headers), Map::class.java)
        assertEquals(200, remResp.statusCode.value())

        // 6) Календарь: день
        val calDay = rest.exchange(url("/calendar/day?date=2030-01-01"), HttpMethod.GET, HttpEntity<Void>(headers), Map::class.java)
        assertEquals(200, calDay.statusCode.value())
    }
}
