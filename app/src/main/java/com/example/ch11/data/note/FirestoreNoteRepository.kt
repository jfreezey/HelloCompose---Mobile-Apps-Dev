package com.example.ch11.data.note

import android.util.Log
import com.example.ch11.data.auth.AuthRepository
import com.example.ch11.data.model.Note
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.Query
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "FirestoreNoteRepo"

// Struktur data:  users/{uid}/notes/{noteId}
@Singleton
class FirestoreNoteRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository
) : NoteRepository {

    private fun notesOf(uid: String): CollectionReference =
        firestore.collection("users").document(uid).collection("notes")

    private fun currentNotes(): CollectionReference =
        notesOf(authRepository.currentUser?.uid ?: throw NotSignedInException())

    // Listener mengikuti status login: ganti akun = ganti collection,
    // logout = daftar kosong (listener lama dilepas oleh flatMapLatest).
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeNotes(): Flow<List<Note>> = authRepository.authState
        .map { it?.uid }
        .distinctUntilChanged()
        .flatMapLatest { uid -> if (uid == null) flowOf(emptyList()) else notesSnapshots(uid) }

    @Suppress("UNUSED_PARAMETER")
    private fun notesSnapshots(uid: String): Flow<List<Note>> =
        callbackFlow {
            val registration = notesOf(uid)
                .orderBy(FIELD_CREATED_AT, Query.Direction.DESCENDING)
                .addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->

                    if (error != null) {
                        close(
                            NoteAccessException(
                                firestoreErrorMessage(error),
                                error
                            )
                        )
                        return@addSnapshotListener
                    }

                    val notes = snapshot?.documents.orEmpty().map { doc ->
                        noteFromMap(
                            id = doc.id,
                            data = doc.data.orEmpty(),
                            hasPendingWrites = doc.metadata.hasPendingWrites()
                        )
                    }

                    trySend(sortNotes(notes))
                }

            // Listener dilepas saat Flow tidak lagi digunakan.
            awaitClose {
                registration.remove()
            }
        }
    // MetadataChanges.INCLUDE: listener juga dipanggil saat server MENGONFIRMASI
    // penulisan, sehingga tanda "Menunggu sinkron" di kartu bisa hilang.

    override suspend fun getNote(id: String): Note? {
        val doc = currentNotes()
            .document(id)
            .get()
            .await()

        return if (doc.exists()) {
            noteFromMap(
                id = doc.id,
                data = doc.data.orEmpty()
            )
        } else {
            null
        }
    }

    override fun addNote(note: Note): Result<String> =
        runCatching {
            val doc = currentNotes().document()

            doc.set(note.toFirestoreMap()).logFailure("add")

            doc.id
        }

    // Memperbarui catatan.
    // Tidak menggunakan await().
    override fun updateNote(note: Note): Result<Unit> =
        runCatching {
            currentNotes()
                .document(note.id)
                .set(note.toFirestoreMap())
                .logFailure("update")
        }

    override fun deleteNote(id: String): Result<Unit> =
        runCatching {
            currentNotes()
                .document(id)
                .delete()
                .logFailure("delete")
        }

    // Sudah disediakan. Penolakan server (mis. Security Rules) baru diketahui belakangan.
    // Saat itu cache lokal dikembalikan dan listener memancarkan data yang benar.
    @Suppress("unused")   // dipakai setelah T3.3
    private fun Task<Void>.logFailure(action: String) {
        addOnFailureListener { e -> Log.w(TAG, "$action ditolak server: ${e.message}") }
    }
}

class NoteAccessException(message: String, cause: Throwable? = null) : Exception(message, cause)

@Suppress("unused")   // dipakai setelah T3.2
private fun firestoreErrorMessage(e: FirebaseFirestoreException): String = when (e.code) {
    FirebaseFirestoreException.Code.PERMISSION_DENIED ->
        "Akses ditolak server. Periksa Security Rules di Firebase Console."
    FirebaseFirestoreException.Code.UNAVAILABLE -> "Server tidak dapat dihubungi."
    else -> e.message ?: "Gagal memuat catatan."
}
