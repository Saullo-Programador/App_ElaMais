package com.example.ela.domain.usecase.auth

import com.example.ela.domain.model.User
import com.example.ela.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthUseCasesTest {

    private val repository = mockk<AuthRepository>()
    private val user = User(uid = "uid-1", email = "ela@email.com", displayName = "Ela")

    // ---------------- Login ----------------

    @Test
    fun `Login deve retornar o usuario quando o repositorio autentica`() = runTest {
        coEvery { repository.login("ela@email.com", "123456") } returns Result.success(user)

        val result = LoginUseCase(repository)("ela@email.com", "123456")

        assertTrue(result.isSuccess)
        assertEquals(user, result.getOrNull())
    }

    @Test
    fun `Login deve repassar a falha do repositorio`() = runTest {
        coEvery { repository.login(any(), any()) } returns Result.failure(Exception("Senha incorreta"))

        val result = LoginUseCase(repository)("ela@email.com", "errada")

        assertTrue(result.isFailure)
        assertEquals("Senha incorreta", result.exceptionOrNull()?.message)
    }

    // ---------------- Signup ----------------

    @Test
    fun `Signup deve criar a conta e retornar o usuario`() = runTest {
        coEvery { repository.signup("nova@email.com", "123456") } returns Result.success(user)

        val result = SignupUseCase(repository)("nova@email.com", "123456")

        assertTrue(result.isSuccess)
        assertEquals("uid-1", result.getOrNull()?.uid)
    }

    @Test
    fun `Signup deve repassar a falha quando o email ja existe`() = runTest {
        coEvery { repository.signup(any(), any()) } returns Result.failure(Exception("E-mail já cadastrado"))

        val result = SignupUseCase(repository)("ela@email.com", "123456")

        assertTrue(result.isFailure)
        assertEquals("E-mail já cadastrado", result.exceptionOrNull()?.message)
    }

    // ---------------- Google ----------------

    @Test
    fun `GoogleLogin deve autenticar usando o idToken`() = runTest {
        coEvery { repository.loginWithGoogle("token-abc") } returns Result.success(user)

        val result = GoogleLoginUseCase(repository)("token-abc")

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { repository.loginWithGoogle("token-abc") }
    }

    @Test
    fun `GoogleLogin deve repassar a falha`() = runTest {
        coEvery { repository.loginWithGoogle(any()) } returns Result.failure(Exception("Token inválido"))

        val result = GoogleLoginUseCase(repository)("token-ruim")

        assertTrue(result.isFailure)
    }

    // ---------------- Logout ----------------

    @Test
    fun `Logout deve retornar sucesso`() = runTest {
        coEvery { repository.logout() } returns Result.success(Unit)

        val result = LogoutUseCase(repository)()

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { repository.logout() }
    }

    @Test
    fun `Logout deve repassar a falha`() = runTest {
        coEvery { repository.logout() } returns Result.failure(Exception("Erro ao sair"))

        val result = LogoutUseCase(repository)()

        assertTrue(result.isFailure)
        assertEquals("Erro ao sair", result.exceptionOrNull()?.message)
    }

    // ---------------- Usuário atual ----------------

    @Test
    fun `GetCurrentUser deve retornar o usuario logado`() {
        every { repository.getCurrentUser() } returns user

        assertEquals(user, GetCurrentUserUseCase(repository)())
    }

    @Test
    fun `GetCurrentUser deve retornar null quando ninguem esta logado`() {
        every { repository.getCurrentUser() } returns null

        val result = GetCurrentUserUseCase(repository)()

        assertNull(result)
    }
}
