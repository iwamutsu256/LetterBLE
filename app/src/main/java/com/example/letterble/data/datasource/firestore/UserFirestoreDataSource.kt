/**
 * UserFirestoreDataSource.kt
 *
 * 役割:
 * - Firestoreとの直接通信
 * - CRUD操作実装
 *
 * 注意:
 * - ロジックを書かない
 */

// TODO: Firestoreインスタンス取得
// TODO: CRUD処理（create, read, update, delete）を書く
// TODO: try-catchで例外処理する
// TODO: suspend関数にする（coroutine対応）

// 例:
// TODO: queryでfilterする
// TODO: documentをdataクラスに変換する
// TODO: timestampを適切な型に変換する

package com.example.letterble.data.datasource.firestore

import com.example.letterble.domain.model.User
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class UserFirestoreDataSource(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val usersCollection = firestore.collection(FirestoreCollections.USERS)

    suspend fun saveUser(user: User) {
        val data = mapOf(
            FirestoreFields.User.USER_NAME to user.userName,
            FirestoreFields.User.CARRYING_LETTER_IDS to user.carryingLetterIds
        )

        usersCollection
            .document(user.userName)
            .set(data)
            .awaitResult()
    }

    suspend fun getUser(userName: String): User? {
        val document = usersCollection
            .document(userName)
            .get()
            .awaitResult()

        if (!document.exists()) {
            return null
        }

        val carryingLetterIds = document
            .get(FirestoreFields.User.CARRYING_LETTER_IDS)
            as? List<*>

        return User(
            userName = document.getString(FirestoreFields.User.USER_NAME).orEmpty(),
            carryingLetterIds = carryingLetterIds
                ?.filterIsInstance<String>()
                ?: emptyList()
        )
    }
}

private suspend fun <T> Task<T>.awaitResult(): T {
    return suspendCancellableCoroutine { continuation ->
        addOnSuccessListener { result ->
            continuation.resume(result)
        }

        addOnFailureListener { exception ->
            continuation.resumeWithException(exception)
        }
    }
}
