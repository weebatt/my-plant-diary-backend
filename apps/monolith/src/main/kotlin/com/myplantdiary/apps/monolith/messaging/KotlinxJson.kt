package com.myplantdiary.apps.monolith.messaging

import kotlinx.serialization.json.Json

object KotlinxJson {
    val json: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = false
    }
}

