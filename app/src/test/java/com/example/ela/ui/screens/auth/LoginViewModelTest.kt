package com.example.ela.ui.screens.auth

import com.example.ela.domain.model.User
import com.example.ela.domain.usecase.auth.GetCurrentUserUseCase
import com.example.ela.domain.usecase.auth.GoogleLoginUseCase
import com.example.ela.domain.usecase.auth.LoginUseCase
import com.example.ela.domain.usecase.auth.LogoutUseCase
import com.example.ela.domain.usecase.auth.SignupUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private val loginUseCase = mockk<LoginUseCase>()
    private val signupUseCase = mockk<SignupUseCase>()
    private val googleLoginUseCase = mockk<GoogleLoginUseCase>()
    private val logoutUseCase = mockk<LogoutUseCase>()
    private val getCurrentUserUseCase = mockk<GetCurrentUserUseCase>()

    private lateinit var testDispatcher: TestDispatcher

    @Before
    fun setup() {
        testDispatcher = UnconfinedTestDispatcher()
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `login should update state to Success when usecase returns success`() = runTest {
        // Given
        val email = "test@email.com"
        val password = "password123"
        val user = User("uid123", email, "Test User")

        coEvery { loginUseCase(email, password) } returns Result.success(user)

        val viewModel = LoginViewModel(loginUseCase, signupUseCase, googleLoginUseCase, logoutUseCase, getCurrentUserUseCase)

        // When
        viewModel.login(email, password)
        advanceUntilIdle()

        // Then
        assertEquals(AuthUiState.Success, viewModel.uiState.value)
    }

    @Test
    fun `login should update state to Error when usecase returns failure`() = runTest {
        // Given
        val email = "test@email.com"
        val password = "wrong_password"
        val errorMessage = "Senha incorreta"

        coEvery { loginUseCase(email, password) } returns Result.failure(Exception(errorMessage))

        val viewModel = LoginViewModel(loginUseCase, signupUseCase, googleLoginUseCase, logoutUseCase, getCurrentUserUseCase)

        // When
        viewModel.login(email, password)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assert(state is AuthUiState.Error)
        assertEquals(errorMessage, (state as AuthUiState.Error).message)
    }

    @Test
    fun `signup should update state to Success when usecase returns success`() = runTest {
        // Given
        val email = "new@email.com"
        val password = "password123"
        val user = User("uid456", email, "New User")

        coEvery { signupUseCase(email, password) } returns Result.success(user)

        val viewModel = LoginViewModel(loginUseCase, signupUseCase, googleLoginUseCase, logoutUseCase, getCurrentUserUseCase)

        // When
        viewModel.signup(email, password)
        advanceUntilIdle()

        // Then
        assertEquals(AuthUiState.Success, viewModel.uiState.value)
    }

    @Test
    fun `signup should update state to Error when usecase returns failure`() = runTest {
        // Given
        val email = "existing@email.com"
        val password = "password123"
        val errorMessage = "E-mail já cadastrado"

        coEvery { signupUseCase(email, password) } returns Result.failure(Exception(errorMessage))

        val viewModel = LoginViewModel(loginUseCase, signupUseCase, googleLoginUseCase, logoutUseCase, getCurrentUserUseCase)

        // When
        viewModel.signup(email, password)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assert(state is AuthUiState.Error)
        assertEquals(errorMessage, (state as AuthUiState.Error).message)
    }

    @Test
    fun `isUserAuthenticated should return true when current user is not null`() {
        // Given
        val user = User("uid123", "test@email.com", "Test User")
        every { getCurrentUserUseCase() } returns user

        val viewModel = LoginViewModel(loginUseCase, signupUseCase, googleLoginUseCase, logoutUseCase, getCurrentUserUseCase)

        // When & Then
        assertEquals(true, viewModel.isUserAuthenticated())
    }

    @Test
    fun `isUserAuthenticated should return false when current user is null`() {
        // Given
        every { getCurrentUserUseCase() } returns null

        val viewModel = LoginViewModel(loginUseCase, signupUseCase, googleLoginUseCase, logoutUseCase, getCurrentUserUseCase)

        // When & Then
        assertEquals(false, viewModel.isUserAuthenticated())
    }

    @Test
    fun `loginWithGoogle should update state to Success when usecase returns success`() = runTest {
        // Given
        val idToken = "google-id-token-123"
        val user = User("uid_google", "google@email.com", "Google User")

        coEvery { googleLoginUseCase(idToken) } returns Result.success(user)

        val viewModel = LoginViewModel(loginUseCase, signupUseCase, googleLoginUseCase, logoutUseCase, getCurrentUserUseCase)

        // When
        viewModel.loginWithGoogle(idToken)
        advanceUntilIdle()

        // Then
        assertEquals(AuthUiState.Success, viewModel.uiState.value)
    }

    @Test
    fun `loginWithGoogle should update state to Error when usecase returns failure`() = runTest {
        // Given
        val idToken = "invalid-google-token"
        val errorMessage = "Erro ao fazer login com Google"

        coEvery { googleLoginUseCase(idToken) } returns Result.failure(Exception(errorMessage))

        val viewModel = LoginViewModel(loginUseCase, signupUseCase, googleLoginUseCase, logoutUseCase, getCurrentUserUseCase)

        // When
        viewModel.loginWithGoogle(idToken)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assert(state is AuthUiState.Error)
        assertEquals(errorMessage, (state as AuthUiState.Error).message)
    }
}
