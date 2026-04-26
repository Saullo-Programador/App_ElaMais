package com.example.ela.data.mapper

import com.example.ela.data.local.entity.PreferencesEntity
import com.example.ela.domain.model.Preferences
import org.junit.Assert.assertEquals
import org.junit.Test

class PreferencesMapperTest {

    @Test
    fun `Preferences toEntity and toDomain should maintain data integrity`() {
        val originalDomain = Preferences(
            id = 1,
            favoriteFoods = listOf("Pizza", "Sushi"),
            notificationsEnabled = true,
            notificationsTime = "10:00",
            timesPerDay = 2
        )

        val entity = originalDomain.toEntity()
        val mappedDomain = entity.toDomain()

        assertEquals(originalDomain.id, mappedDomain.id)
        assertEquals(originalDomain.favoriteFoods, mappedDomain.favoriteFoods)
        assertEquals(originalDomain.notificationsEnabled, mappedDomain.notificationsEnabled)
        assertEquals(originalDomain.notificationsTime, mappedDomain.notificationsTime)
        assertEquals(originalDomain.timesPerDay, mappedDomain.timesPerDay)
    }
}
