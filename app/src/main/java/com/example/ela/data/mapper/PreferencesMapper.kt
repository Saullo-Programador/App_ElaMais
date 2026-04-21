package com.example.ela.data.mapper

import com.example.ela.core.utils.JsonUtils
import com.example.ela.data.local.entity.PreferencesEntity
import com.example.ela.data.remote.dto.PreferencesDto
import com.example.ela.domain.model.Preferences
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

fun PreferencesEntity.toDomain(): Preferences {
    return Preferences(
        id = id,
        favoriteFoods = JsonUtils.decode(favoriteFoods),
        favoriteSweets = JsonUtils.decode(favoriteSweets),
        dislikedThings = JsonUtils.decode(dislikedThings),
        symptoms = JsonUtils.decode(symptoms)
    )
}

fun Preferences.toEntity(): PreferencesEntity {
    return PreferencesEntity(
        id = id,
        favoriteFoods = JsonUtils.encode(favoriteFoods),
        favoriteSweets = JsonUtils.encode(favoriteSweets),
        dislikedThings = JsonUtils.encode(dislikedThings),
        symptoms = JsonUtils.encode(symptoms)
    )
}

fun PreferencesDto.toDomain(): Preferences {
    return Preferences(
        id = id.hashCode().toLong(),
        favoriteFoods = favoriteFoods,
        favoriteSweets = favoriteSweets,
        dislikedThings = dislikedThings,
        symptoms = symptoms
    )
}

fun Preferences.toDto(): PreferencesDto {
    return PreferencesDto(
        id = id.toString(),
        favoriteFoods = favoriteFoods,
        favoriteSweets = favoriteSweets,
        dislikedThings = dislikedThings,
        symptoms = symptoms
    )
}