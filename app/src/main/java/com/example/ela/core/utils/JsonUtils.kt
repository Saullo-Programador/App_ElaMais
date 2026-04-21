package com.example.ela.core.utils

import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

object JsonUtils {

    private val json = Json {
        ignoreUnknownKeys = true
    }

    fun encode(list: List<String>): String {
        return json.encodeToString(list)
    }

    fun decode(jsonString: String): List<String> {
        return json.decodeFromString(jsonString)
    }
}