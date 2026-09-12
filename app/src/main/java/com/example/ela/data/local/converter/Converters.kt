package com.example.ela.data.local.converter

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class Converters {

    @TypeConverter
    fun fromList(value: List<String>): String {
        return Json.encodeToString(value)
    }

    @TypeConverter
    fun toList(value: String): List<String> {
        return Json.decodeFromString(value)
    }
}