package com.example.ela.data.repository

import com.example.ela.domain.model.Couple
import com.example.ela.domain.model.Invite
import com.example.ela.domain.model.InviteStatus
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AuthRepositoryImplTest {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var repository: AuthRepositoryImpl

    private fun fakeUser(uid: String = "uid-1", email: String = "ela@email.com", name: String = "Ela") =
        mockk<FirebaseUser>().also {
            every { it.uid } returns uid
            every { it.email } returns email
            every { it.displayName } returns name
        }

    private fun authResult(user: FirebaseUser?) = mockk<AuthResult>().also {
        every { it.user } returns user
    }

    @Before
    fun setup() {
        firebaseAuth = mockk()
        firestore = mockk()
        repository = AuthRepositoryImpl(firebaseAuth, firestore)
    }

    // ------------------------------------------------------------------
    // login
    // ------------------------------------------------------------------

    @Test
    fun `login deve retornar o usuario quando o firebase autentica`() = runTest {
        every {
            firebaseAuth.signInWithEmailAndPassword("ela@email.com", "123456")
        } returns Tasks.forResult(authResult(fakeUser()))

        val result = repository.login("ela@email.com", "123456")

        assertTrue(result.isSuccess)
        assertEquals("uid-1", result.getOrNull()?.uid)
        assertEquals("ela@email.com", result.getOrNull()?.email)
    }

    @Test
    fun `login deve falhar quando o firebase nao retorna usuario`() = runTest {
        every {
            firebaseAuth.signInWithEmailAndPassword(any(), any())
        } returns Tasks.forResult(authResult(null))

        val result = repository.login("ela@email.com", "123456")

        assertTrue(result.isFailure)
    }

    @Test
    fun `login deve repassar a excecao do firebase como falha`() = runTest {
        every {
            firebaseAuth.signInWithEmailAndPassword(any(), any())
        } returns Tasks.forException(RuntimeException("Senha incorreta"))

        val result = repository.login("ela@email.com", "errada")

        assertTrue(result.isFailure)
        assertEquals("Senha incorreta", result.exceptionOrNull()?.message)
    }

    // ------------------------------------------------------------------
    // signup
    // ------------------------------------------------------------------

    @Test
    fun `signup deve retornar o usuario criado`() = runTest {
        every {
            firebaseAuth.createUserWithEmailAndPassword("nova@email.com", "123456")
        } returns Tasks.forResult(authResult(fakeUser(uid = "uid-2", email = "nova@email.com")))

        val result = repository.signup("nova@email.com", "123456")

        assertTrue(result.isSuccess)
        assertEquals("uid-2", result.getOrNull()?.uid)
    }

    @Test
    fun `signup deve repassar a falha quando o email ja existe`() = runTest {
        every {
            firebaseAuth.createUserWithEmailAndPassword(any(), any())
        } returns Tasks.forException(RuntimeException("E-mail já cadastrado"))

        val result = repository.signup("ela@email.com", "123456")

        assertTrue(result.isFailure)
        assertEquals("E-mail já cadastrado", result.exceptionOrNull()?.message)
    }

    // ------------------------------------------------------------------
    // loginWithGoogle
    // ------------------------------------------------------------------

    @Test
    fun `loginWithGoogle deve autenticar com a credencial do google`() = runTest {
        mockkStatic(GoogleAuthProvider::class)
        try {
            val credential = mockk<com.google.firebase.auth.AuthCredential>()
            every { GoogleAuthProvider.getCredential("token-abc", null) } returns credential
            every { firebaseAuth.signInWithCredential(credential) } returns Tasks.forResult(authResult(fakeUser()))

            val result = repository.loginWithGoogle("token-abc")

            assertTrue(result.isSuccess)
            assertEquals("uid-1", result.getOrNull()?.uid)
        } finally {
            unmockkStatic(GoogleAuthProvider::class)
        }
    }

    @Test
    fun `loginWithGoogle deve falhar quando o firebase nao retorna usuario`() = runTest {
        mockkStatic(GoogleAuthProvider::class)
        try {
            val credential = mockk<com.google.firebase.auth.AuthCredential>()
            every { GoogleAuthProvider.getCredential(any(), null) } returns credential
            every { firebaseAuth.signInWithCredential(credential) } returns Tasks.forResult(authResult(null))

            val result = repository.loginWithGoogle("token-ruim")

            assertTrue(result.isFailure)
        } finally {
            unmockkStatic(GoogleAuthProvider::class)
        }
    }

    // ------------------------------------------------------------------
    // logout
    // ------------------------------------------------------------------

    @Test
    fun `logout deve retornar sucesso`() = runTest {
        every { firebaseAuth.signOut() } returns Unit

        val result = repository.logout()

        assertTrue(result.isSuccess)
    }

    // ------------------------------------------------------------------
    // getCurrentUser
    // ------------------------------------------------------------------

    @Test
    fun `getCurrentUser deve retornar o usuario logado`() {
        every { firebaseAuth.currentUser } returns fakeUser()

        val result = repository.getCurrentUser()

        assertEquals("uid-1", result?.uid)
    }

    @Test
    fun `getCurrentUser deve retornar null quando ninguem esta logado`() {
        every { firebaseAuth.currentUser } returns null

        assertNull(repository.getCurrentUser())
    }

    // ------------------------------------------------------------------
    // sendInvite
    // ------------------------------------------------------------------

    @Test
    fun `sendInvite deve salvar o convite com o id gerado pelo firestore`() = runTest {
        val invites = mockk<CollectionReference>()
        val newDoc = mockk<DocumentReference>()
        every { firestore.collection("invites") } returns invites
        every { invites.document() } returns newDoc
        every { newDoc.id } returns "invite-1"
        every { invites.document("invite-1") } returns newDoc
        every { newDoc.set(any()) } returns Tasks.forResult(null)

        val invite = Invite(senderId = "uid-1", receiverEmail = "parceiro@email.com")
        val result = repository.sendInvite(invite)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `sendInvite deve repassar a falha do firestore`() = runTest {
        val invites = mockk<CollectionReference>()
        val newDoc = mockk<DocumentReference>()
        every { firestore.collection("invites") } returns invites
        every { invites.document() } returns newDoc
        every { newDoc.id } returns "invite-1"
        every { invites.document("invite-1") } returns newDoc
        every { newDoc.set(any()) } returns Tasks.forException(RuntimeException("Sem conexão"))

        val result = repository.sendInvite(Invite(senderId = "uid-1", receiverEmail = "parceiro@email.com"))

        assertTrue(result.isFailure)
        assertEquals("Sem conexão", result.exceptionOrNull()?.message)
    }

    // ------------------------------------------------------------------
    // getPendingInvites
    // ------------------------------------------------------------------

    @Test
    fun `getPendingInvites deve retornar apenas convites pendentes do email`() = runTest {
        val invite = Invite(inviteId = "i1", senderId = "uid-2", receiverEmail = "ela@email.com")
        val invites = mockk<CollectionReference>()
        val afterEmail = mockk<Query>()
        val afterStatus = mockk<Query>()
        val doc = documentSnapshot("i1")

        every { firestore.collection("invites") } returns invites
        every { invites.whereEqualTo("receiverEmail", "ela@email.com") } returns afterEmail
        every { afterEmail.whereEqualTo("status", InviteStatus.PENDING.name) } returns afterStatus
        every { afterStatus.get() } returns Tasks.forResult(querySnapshot(doc))
        every { doc.toObject(Invite::class.java) } returns invite

        val result = repository.getPendingInvites("ela@email.com")

        assertEquals(listOf(invite), result.getOrNull())
    }

    // ------------------------------------------------------------------
    // acceptInvite
    // ------------------------------------------------------------------

    @Test
    fun `acceptInvite deve falhar quando o convite nao existe`() = runTest {
        val invites = mockk<CollectionReference>()
        val inviteDoc = mockk<DocumentReference>()
        val snapshot = mockk<DocumentSnapshot>()

        every { firestore.collection("invites") } returns invites
        every { invites.document("i1") } returns inviteDoc
        every { inviteDoc.get() } returns Tasks.forResult(snapshot)
        every { snapshot.toObject(Invite::class.java) } returns null

        val result = repository.acceptInvite("i1", "uid-2")

        assertTrue(result.isFailure)
        assertEquals("Convite não encontrado", result.exceptionOrNull()?.message)
    }

    @Test
    fun `acceptInvite deve criar o casal e atualizar o status do convite`() = runTest {
        val invite = Invite(inviteId = "i1", senderId = "uid-1", receiverEmail = "ela@email.com")
        val invites = mockk<CollectionReference>()
        val inviteDoc = mockk<DocumentReference>()
        val snapshot = mockk<DocumentSnapshot>()
        val couples = mockk<CollectionReference>()
        val newCoupleDoc = mockk<DocumentReference>()

        every { firestore.collection("invites") } returns invites
        every { invites.document("i1") } returns inviteDoc
        every { inviteDoc.get() } returns Tasks.forResult(snapshot)
        every { snapshot.toObject(Invite::class.java) } returns invite

        every { firestore.collection("couples") } returns couples
        every { couples.document() } returns newCoupleDoc
        every { newCoupleDoc.id } returns "couple-1"
        every { couples.document("couple-1") } returns newCoupleDoc
        every { newCoupleDoc.set(any<Couple>()) } returns Tasks.forResult(null)

        every { inviteDoc.update("status", InviteStatus.ACCEPTED.name) } returns Tasks.forResult(null)

        val result = repository.acceptInvite("i1", "uid-2")

        assertTrue(result.isSuccess)
        assertEquals("uid-1", result.getOrNull()?.partner1Id)
        assertEquals("uid-2", result.getOrNull()?.partner2Id)
    }

    // ------------------------------------------------------------------
    // getCoupleByUserId
    // ------------------------------------------------------------------

    @Test
    fun `getCoupleByUserId deve encontrar o casal quando o usuario e o parceiro1`() = runTest {
        val couple = Couple(coupleId = "c1", partner1Id = "uid-1", partner2Id = "uid-2")
        val couples = mockk<CollectionReference>()
        val query = mockk<Query>()
        val doc = documentSnapshot("c1")

        every { firestore.collection("couples") } returns couples
        every { couples.whereEqualTo("partner1Id", "uid-1") } returns query
        every { query.get() } returns Tasks.forResult(querySnapshot(doc))
        every { doc.toObject(Couple::class.java) } returns couple

        val result = repository.getCoupleByUserId("uid-1")

        assertEquals(couple, result.getOrNull())
    }

    @Test
    fun `getCoupleByUserId deve buscar como parceiro2 quando nao e parceiro1`() = runTest {
        val couple = Couple(coupleId = "c1", partner1Id = "uid-2", partner2Id = "uid-1")
        val couples = mockk<CollectionReference>()
        val query1 = mockk<Query>()
        val query2 = mockk<Query>()
        val doc = documentSnapshot("c1")

        every { firestore.collection("couples") } returns couples
        every { couples.whereEqualTo("partner1Id", "uid-1") } returns query1
        every { query1.get() } returns Tasks.forResult(querySnapshot())
        every { couples.whereEqualTo("partner2Id", "uid-1") } returns query2
        every { query2.get() } returns Tasks.forResult(querySnapshot(doc))
        every { doc.toObject(Couple::class.java) } returns couple

        val result = repository.getCoupleByUserId("uid-1")

        assertEquals(couple, result.getOrNull())
    }

    @Test
    fun `getCoupleByUserId deve retornar sucesso com null quando o usuario nao tem casal`() = runTest {
        val couples = mockk<CollectionReference>()
        val query1 = mockk<Query>()
        val query2 = mockk<Query>()

        every { firestore.collection("couples") } returns couples
        every { couples.whereEqualTo("partner1Id", "uid-1") } returns query1
        every { query1.get() } returns Tasks.forResult(querySnapshot())
        every { couples.whereEqualTo("partner2Id", "uid-1") } returns query2
        every { query2.get() } returns Tasks.forResult(querySnapshot())

        val result = repository.getCoupleByUserId("uid-1")

        assertTrue(result.isSuccess)
        assertNull(result.getOrNull())
    }

    // ------------------------------------------------------------------
    // updateSharingSettings
    // ------------------------------------------------------------------

    @Test
    fun `updateSharingSettings deve atualizar o documento do casal`() = runTest {
        val couples = mockk<CollectionReference>()
        val document = mockk<DocumentReference>()
        every { firestore.collection("couples") } returns couples
        every { couples.document("c1") } returns document
        every { document.set(any(), any<com.google.firebase.firestore.SetOptions>()) } returns Tasks.forResult(null)

        val result = repository.updateSharingSettings("c1", com.example.ela.domain.model.SharingSettings())

        assertTrue(result.isSuccess)
    }
}
