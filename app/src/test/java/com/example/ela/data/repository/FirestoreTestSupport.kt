package com.example.ela.data.repository

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import io.mockk.every
import io.mockk.mockk

/**
 * Utilitários compartilhados pelos testes dos repositórios.
 *
 * Os repositórios gravam em  users/{uid}/{coleção}/{documento}.
 * [FirestoreUserCollectionMock] monta essa cadeia de mocks uma única vez.
 */

fun okTask(): Task<Void> = Tasks.forResult<Void>(null)

/** Task do Firebase já concluída com erro. */
fun failedTask(message: String = "Firebase Error"): Task<Void> =
    Tasks.forException<Void>(RuntimeException(message))

/** Task do Firebase já concluída com erro para tipos genéricos. */
fun <T> failedTaskGeneric(message: String = "Firebase Error"): Task<T> =
    Tasks.forException<T>(RuntimeException(message))

/** DocumentSnapshot simples que só conhece o próprio id. */
fun documentSnapshot(id: String): DocumentSnapshot {
    val snapshot = mockk<DocumentSnapshot>()
    every { snapshot.id } returns id
    return snapshot
}

/** QuerySnapshot com os documentos informados. */
fun querySnapshot(vararg documents: DocumentSnapshot): QuerySnapshot {
    val snapshot = mockk<QuerySnapshot>()
    every { snapshot.documents } returns documents.toList()
    return snapshot
}

class FirestoreUserCollectionMock(
    firestore: FirebaseFirestore,
    uid: String,
    collectionName: String
) {
    val collection: CollectionReference = mockk(relaxed = true)
    private val userDocument: DocumentReference = mockk(relaxed = true)

    init {
        val users = mockk<CollectionReference>(relaxed = true)
        every { firestore.collection("users") } returns users
        every { users.document(uid) } returns userDocument
        every { userDocument.collection(collectionName) } returns collection
    }

    fun document(id: String): DocumentReference {
        val document = mockk<DocumentReference>(relaxed = true)
        // Important: the repository calls collection.document(id),
        // so we must stub that call to return our mock document.
        every { collection.document(id) } returns document
        every { collection.document(any()) } returns document
        return document
    }
}
