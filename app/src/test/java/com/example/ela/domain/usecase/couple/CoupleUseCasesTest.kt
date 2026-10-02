package com.example.ela.domain.usecase.couple

import com.example.ela.domain.model.Couple
import com.example.ela.domain.model.Invite
import com.example.ela.domain.model.InviteStatus
import com.example.ela.domain.model.SharingSettings
import com.example.ela.domain.model.User
import com.example.ela.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CoupleUseCasesTest {

    private val repository = mockk<AuthRepository>()
    private val user = User(uid = "uid-1", email = "ela@email.com")

    // ---------------- SendInvite ----------------

    @Test
    fun `SendInvite deve enviar convite com o remetente logado e o email do destinatario`() = runTest {
        every { repository.getCurrentUser() } returns user
        val sent = slot<Invite>()
        coEvery { repository.sendInvite(capture(sent)) } returns Result.success(Unit)

        val result = SendInviteUseCase(repository)("parceiro@email.com")

        assertTrue(result.isSuccess)
        assertEquals("uid-1", sent.captured.senderId)
        assertEquals("parceiro@email.com", sent.captured.receiverEmail)
        assertEquals(InviteStatus.PENDING, sent.captured.status)
    }

    @Test
    fun `SendInvite deve falhar quando o usuario nao esta autenticado`() = runTest {
        every { repository.getCurrentUser() } returns null

        val result = SendInviteUseCase(repository)("parceiro@email.com")

        assertTrue(result.isFailure)
        assertEquals("Usuário não autenticado", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { repository.sendInvite(any()) }
    }

    @Test
    fun `SendInvite deve repassar a falha do repositorio`() = runTest {
        every { repository.getCurrentUser() } returns user
        coEvery { repository.sendInvite(any()) } returns Result.failure(Exception("Sem conexão"))

        val result = SendInviteUseCase(repository)("parceiro@email.com")

        assertTrue(result.isFailure)
        assertEquals("Sem conexão", result.exceptionOrNull()?.message)
    }

    // ---------------- GetPendingInvites ----------------

    @Test
    fun `GetPendingInvites deve retornar os convites do email`() = runTest {
        val invites = listOf(Invite(inviteId = "i1", senderId = "uid-2", receiverEmail = "ela@email.com"))
        coEvery { repository.getPendingInvites("ela@email.com") } returns Result.success(invites)

        val result = GetPendingInvitesUseCase(repository)("ela@email.com")

        assertEquals(invites, result.getOrNull())
    }

    @Test
    fun `GetPendingInvites deve retornar lista vazia quando nao ha convites`() = runTest {
        coEvery { repository.getPendingInvites(any()) } returns Result.success(emptyList())

        val result = GetPendingInvitesUseCase(repository)("ela@email.com")

        assertTrue(result.getOrThrow().isEmpty())
    }

    // ---------------- AcceptInvite ----------------

    @Test
    fun `AcceptInvite deve aceitar usando o id do usuario logado`() = runTest {
        val couple = Couple(coupleId = "c1", partner1Id = "uid-2", partner2Id = "uid-1")
        every { repository.getCurrentUser() } returns user
        coEvery { repository.acceptInvite("i1", "uid-1") } returns Result.success(couple)

        val result = AcceptInviteUseCase(repository)("i1")

        assertEquals(couple, result.getOrNull())
        coVerify(exactly = 1) { repository.acceptInvite("i1", "uid-1") }
    }

    @Test
    fun `AcceptInvite deve falhar quando o usuario nao esta autenticado`() = runTest {
        every { repository.getCurrentUser() } returns null

        val result = AcceptInviteUseCase(repository)("i1")

        assertTrue(result.isFailure)
        assertEquals("Usuário não autenticado", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { repository.acceptInvite(any(), any()) }
    }

    // ---------------- GetCouple ----------------

    @Test
    fun `GetCouple deve retornar o casal do usuario`() = runTest {
        val couple = Couple(coupleId = "c1", partner1Id = "uid-1", partner2Id = "uid-2")
        coEvery { repository.getCoupleByUserId("uid-1") } returns Result.success(couple)

        val result = GetCoupleUseCase(repository)("uid-1")

        assertEquals(couple, result.getOrNull())
    }

    @Test
    fun `GetCouple deve retornar null quando o usuario nao tem casal`() = runTest {
        coEvery { repository.getCoupleByUserId(any()) } returns Result.success(null)

        val result = GetCoupleUseCase(repository)("uid-1")

        assertTrue(result.isSuccess)
        assertNull(result.getOrNull())
    }

    // ---------------- UpdateSharingSettings ----------------

    @Test
    fun `UpdateSharingSettings deve enviar as configuracoes ao repositorio`() = runTest {
        val settings = SharingSettings(shareCycleData = true, shareNotes = false, shareHealthMetrics = true)
        coEvery { repository.updateSharingSettings("c1", settings) } returns Result.success(Unit)

        val result = UpdateSharingSettingsUseCase(repository)("c1", settings)

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { repository.updateSharingSettings("c1", settings) }
    }

    @Test
    fun `UpdateSharingSettings deve repassar a falha`() = runTest {
        coEvery { repository.updateSharingSettings(any(), any()) } returns Result.failure(Exception("Sem permissão"))

        val result = UpdateSharingSettingsUseCase(repository)("c1", SharingSettings())

        assertTrue(result.isFailure)
    }
}
