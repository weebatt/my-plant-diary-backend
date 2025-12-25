package com.myplantdiary.apps.telegram

import org.junit.jupiter.api.Assertions.assertEquals
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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = [
        "security.auth.enabled=false",
        "APP_MESSAGING_ENABLED=false"
    ])
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TelegramAdapterE2eTest {
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

    @Test
    fun admin_registry_upsert_list() {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        val upsert = rest.exchange(url("/admin/chat-registry"), HttpMethod.POST, HttpEntity(mapOf(
            "userId" to "user-1",
            "chatId" to 123456L
        ), headers), Map::class.java)
        assertEquals(200, upsert.statusCode.value())

        val list: ResponseEntity<Map<*, *>> = rest.getForEntity(url("/admin/chat-registry"), Map::class.java)
        assertEquals(200, list.statusCode.value())
    }
}

