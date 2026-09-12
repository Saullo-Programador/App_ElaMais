package com.example.ela.ui.screens.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.ela.domain.model.CycleInfo
import com.example.ela.domain.model.CyclePhase
import com.example.ela.ui.theme.ElaTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun homeScreen_whenStateIsEmpty_showsEmptyMessage() {

        composeTestRule.setContent {
            ElaTheme {
                HomeContent(
                    state = HomeUiState(
                        cycleInfo = null,
                        isLoading = false
                    ),
                    onGoToCare = {},
                    onOpenCalendar = {},
                    onPeriodStarted = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText("Cadastre seu ciclo para começar")
            .assertIsDisplayed()
    }

    @Test
    fun homeScreen_whenStateHasData_showsCurrentPhase() {

        val mockInfo = CycleInfo(
            currentPhase = CyclePhase.MENSTRUAL,
            daysUntilNextPeriod = 25,
            isFertileWindow = false,
            isPms = false,
            suggestions = listOf("Beber água"),
            hasData = true
        )

        composeTestRule.setContent {
            ElaTheme {
                HomeContent(
                    state = HomeUiState(
                        cycleInfo = mockInfo,
                        isLoading = false
                    ),
                    onGoToCare = {},
                    onOpenCalendar = {},
                    onPeriodStarted = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText("Período Menstrual")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("25 dias")
            .assertIsDisplayed()
    }

    @Test
    fun homeScreen_whenCareButtonClicked_callsOnGoToCare() {

        var buttonClicked = false

        val mockInfo = CycleInfo(
            currentPhase = CyclePhase.MENSTRUAL,
            daysUntilNextPeriod = 25,
            isFertileWindow = false,
            isPms = false,
            suggestions = listOf("Beber água"),
            hasData = true
        )

        composeTestRule.setContent {
            ElaTheme {
                HomeContent(
                    state = HomeUiState(
                        cycleInfo = mockInfo,
                        isLoading = false
                    ),
                    onGoToCare = {
                        buttonClicked = true
                    },
                    onOpenCalendar = {},
                    onPeriodStarted = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText("Ver cuidados recomendados ❤️")
            .performClick()

        assertEquals(true, buttonClicked)
    }
}