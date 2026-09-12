package com.example.ela.ui.screens.care

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.ela.domain.model.CareAction
import com.example.ela.domain.model.CyclePhase
import com.example.ela.ui.theme.ElaTheme
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class CareScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun careScreen_whenStateHasActions_showsPhaseAndActions() {
        val mockState = CareActionUiState(
            phase = CyclePhase.MENSTRUAL,
            actions = listOf(
                CareAction(1, "Beber Água", "Hidrate-se", CyclePhase.MENSTRUAL, false),
                CareAction(2, "Descansar", "Relaxe", CyclePhase.MENSTRUAL, true)
            ),
            isLoading = false
        )

        composeTestRule.setContent {
            ElaTheme {
                CareScreenContent(
                    state = mockState,
                    onToggle = {},
                    onSave = {}
                )
            }
        }

        // Verifica título da fase
        composeTestRule.onNodeWithText("Período Menstrual").assertIsDisplayed()
        // Verifica descrição da fase
        composeTestRule.onNodeWithText("Momento de cuidar de si. Priorize descanso e conforto.").assertIsDisplayed()
        // Verifica se as ações aparecem
        composeTestRule.onNodeWithText("Beber Água").assertIsDisplayed()
        composeTestRule.onNodeWithText("Descansar").assertIsDisplayed()
        // Verifica o progresso (1/2)
        composeTestRule.onNodeWithText("1/2").assertIsDisplayed()
    }

    @Test
    fun careScreen_whenActionToggled_callsOnToggle() {
        var toggledActionId = -1
        val mockState = CareActionUiState(
            phase = CyclePhase.MENSTRUAL,
            actions = listOf(
                CareAction(1, "Beber Água", "Hidrate-se", CyclePhase.MENSTRUAL, false)
            ),
            isLoading = false
        )

        composeTestRule.setContent {
            ElaTheme {
                CareScreenContent(
                    state = mockState,
                    onToggle = { toggledActionId = it.id.toInt() },
                    onSave = {}
                )
            }
        }

        // Clica na ação para marcar como feita
        composeTestRule.onNodeWithText("Beber Água").performClick()

        assertEquals(1, toggledActionId)
    }

    @Test
    fun careScreen_whenFabClicked_showsAddDialog() {
        val mockState = CareActionUiState(
            phase = CyclePhase.MENSTRUAL,
            actions = emptyList(),
            isLoading = false
        )

        composeTestRule.setContent {
            ElaTheme {
                CareScreenContent(
                    state = mockState,
                    onToggle = {},
                    onSave = {}
                )
            }
        }

        // Clica no FAB para adicionar
        composeTestRule.onNodeWithContentDescription("Adicionar novos Cuidados").performClick()

        // Verifica se o diálogo apareceu
        composeTestRule.onNodeWithText("Novo Cuidados").assertIsDisplayed()
    }

    @Test
    fun careScreen_whenNewActionSaved_callsOnSave() {
        var savedAction: CareAction? = null
        val mockState = CareActionUiState(
            phase = CyclePhase.MENSTRUAL,
            actions = emptyList(),
            isLoading = false
        )

        composeTestRule.setContent {
            ElaTheme {
                CareScreenContent(
                    state = mockState,
                    onToggle = {},
                    onSave = { savedAction = it }
                )
            }
        }

        // Abre diálogo
        composeTestRule.onNodeWithContentDescription("Adicionar novos Cuidados").performClick()

        composeTestRule
            .onNodeWithText("Título")
            .performTextInput("Beber água")

        composeTestRule
            .onNodeWithText("Descrição (opcional)")
            .performTextInput("Hidrate-se bastante")

        composeTestRule
            .onNodeWithText("Salvar")
            .performClick()

        Assert.assertNotNull(savedAction)
        assertEquals("Beber água", savedAction?.title)
        assertEquals("Hidrate-se bastante", savedAction?.description)
        assertEquals(CyclePhase.MENSTRUAL, savedAction?.phase)
        assertEquals(false, savedAction?.isCompleted)
    }
}
