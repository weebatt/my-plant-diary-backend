package com.myplantdiary.apps.monolith.dictionary

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
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
        "APP_MESSAGING_ENABLED=false",
        "SEED_ADMIN_EMAIL=admin@example.com",
        "SEED_ADMIN_PASSWORD=admin123"
    ])
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DictionaryCrudE2eTest {
    companion object {
        @JvmStatic
        private val pg = PostgreSQLContainer<Nothing>("postgres:16").apply { start() }

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

    private fun loginAdmin(): String {
        val resp = rest.postForEntity(url("/auth/login"), mapOf(
            "email" to "admin@example.com",
            "password" to "admin123"
        ), Map::class.java)
        return resp.body?.get("token") as String
    }

    @Test
    fun admin_crud_flow() {
        val token = loginAdmin()
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(token)

        // create
        val latinName = "Testus${UUID.randomUUID()}"
        val created = rest.exchange(url("/admin/plants"), HttpMethod.POST, HttpEntity(mapOf(
            "latinName" to latinName,
            "commonName" to "Тест",
            "light" to "bright"
        ), headers), Map::class.java)
        assertEquals(200, created.statusCode.value())
        val id = created.body?.get("id") as String

        // get
        val get = rest.exchange(url("/admin/plants/$id"), HttpMethod.GET, HttpEntity<Void>(headers), Map::class.java)
        assertEquals(200, get.statusCode.value())
        assertEquals(latinName, get.body?.get("latinName"))

        // update
        val updated = rest.exchange(url("/admin/plants/$id"), HttpMethod.PATCH, HttpEntity(mapOf(
            "commonName" to "Тест-2"
        ), headers), Map::class.java)
        assertEquals(200, updated.statusCode.value())
        assertEquals("Тест-2", updated.body?.get("commonName"))

        // delete
        val deleted = rest.exchange(url("/admin/plants/$id"), HttpMethod.DELETE, HttpEntity<Void>(headers), Map::class.java)
        assertEquals(200, deleted.statusCode.value())
    }
}

