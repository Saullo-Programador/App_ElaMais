package com.example.ela.ui.screens.preferences

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.ela.domain.model.Cycle
import com.example.ela.domain.model.Preferences
import com.example.ela.ui.theme.ElaTheme
import org.junit.Rule
import org.junit.Test

class PreferencesScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun preferencesScreen_showsSections() {
        composeTestRule.setContent {
            ElaTheme {
                PreferencesContent(
                    preferences = null,
                    cycle = null,
                    onSavePreferences = {},
                    onSaveCycle = {}
                )
            }
        }

        // Verifica se as seções principais estão visíveis
        composeTestRule.onNodeWithText("Preferências").assertIsDisplayed()
        composeTestRule.onNodeWithText("Configurações do Ciclo").assertIsDisplayed()
        composeTestRule.onNodeWithText("Personalização").assertIsDisplayed()
    }

    @Test
    fun preferencesScreen_savesCycleWhenButtonClicked() {
        var savedCycle: Cycle? = null
        composeTestRule.setContent {
            ElaTheme {
                PreferencesContent(
                    preferences = null,
                    cycle = null,
                    onSavePreferences = {},
                    onSaveCycle = { savedCycle = it }
                )
            }
        }

        composeTestRule.onNodeWithText("Salvar Ciclo").performClick()

        // Verifica se a função de salvar foi chamada (com valores default)
        assert(savedCycle != null)
    }

    @Test
    fun preferencesScreen_savesPreferencesWhenButtonClicked() {
        var savedPrefs: Preferences? = null
        composeTestRule.setContent {
            ElaTheme {
                PreferencesContent(
                    preferences = null,
                    cycle = null,
                    onSavePreferences = { savedPrefs = it },
                    onSaveCycle = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Salvar Preferências").performClick()

        // Verifica se a função de salvar foi chamada
        assert(savedPrefs != null)
    }
}

