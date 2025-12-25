package com.myplantdiary.apps.monolith

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@WebMvcTest(HealthController::class)
class HealthControllerTest(@Autowired val mockMvc: MockMvc) {
    @Test
    fun `GET /healthz returns ok`() {
        mockMvc.get("/healthz")
            .andExpect {
                status { isOk() }
            }
    }
}

