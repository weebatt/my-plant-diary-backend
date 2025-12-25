package com.myplantdiary.apps.monolith.openapi

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {
    @Bean
    fun customOpenAPI(): OpenAPI = OpenAPI()
        .components(Components())
        .info(
            Info()
                .title("My Plant Diary — Monolith API")
                .version("v1")
                .description("REST API для монолита: пользователи, словарь, дневник, календарь")
        )
}

