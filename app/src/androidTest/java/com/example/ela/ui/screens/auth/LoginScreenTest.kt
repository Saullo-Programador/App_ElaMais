package com.example.ela.ui.screens.auth

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.ela.ui.theme.ElaTheme
import org.junit.Rule
import org.junit.Test

class LoginScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun loginScreen_shouldDisplayInitialComponents() {
        composeTestRule.setContent {
            ElaTheme {
                LoginScreenContent(
                    uiState = AuthUiState.Idle,
                    onLogin = { _, _ -> },
                    onGoogleLogin = { _ -> },
                    onSignup = { },
                    onForgotPassword = { }
                )
            }
        }

        // Verifica se o título de boas-vindas está visível
        composeTestRule.onNodeWithText("Bem-vinda ao ElaMais").assertIsDisplayed()

        // Verifica se o botão de entrar está visível
        composeTestRule.onNodeWithText("Entrar").assertIsDisplayed()

        // Verifica se o botão do Google está visível
        composeTestRule.onNodeWithText("Entrar com Google").assertIsDisplayed()
    }

    @Test
    fun loginScreen_shouldCallOnLogin_withCorrectData_whenButtonIsClicked() {
        var loginCalled = false
        var capturedEmail = ""
        var capturedPassword = ""

        composeTestRule.setContent {
            ElaTheme {
                LoginScreenContent(
                    uiState = AuthUiState.Idle,
                    onLogin = { email, password ->
                        loginCalled = true
                        capturedEmail = email
                        capturedPassword = password
                    },
                    onGoogleLogin = { _ -> },
                    onSignup = { },
                    onForgotPassword = { }
                )
            }
        }

        // Preenche e-mail
        composeTestRule.onNodeWithTag("email_field").performTextInput("test@email.com")
        // Preenche senha
        composeTestRule.onNodeWithTag("password_field").performTextInput("password123")

        // Clica no botão de entrar
        composeTestRule.onNodeWithText("Entrar").performClick()

        // Verifica se o callback de login foi disparado com os dados corretos
        assert(loginCalled)
        org.junit.Assert.assertEquals("test@email.com", capturedEmail)
        org.junit.Assert.assertEquals("password123", capturedPassword)
    }
}
