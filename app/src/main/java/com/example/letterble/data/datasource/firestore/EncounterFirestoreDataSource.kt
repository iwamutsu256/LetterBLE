/**
 * Firestore の ENCOUNTERS コレクションを直接読み書きする DataSource。
 *
 * Encounter モデルと Firestore の Map 変換、すれ違い履歴の保存・
 * 同じユーザー同士の最新すれ違い取得を担当する。
 */
package com.example.letterble.data.datasource.firestore

import com.example.letterble.domain.model.Encounter
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * ENCOUNTERS コレクション専用の DataSource。
 *
 * Relay 処理では、同じ相手と短時間に何度もすれ違い処理をしないため、
 * 直近のすれ違い記録をここから取得する。
 */
class EncounterFirestoreDataSource(
    // テスト時に差し替えられるよう、Firestore 本体はコンストラクタで受け取る。
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    // すれ違い履歴を保存・取得するための ENCOUNTERS コレクション参照。
    private val encountersCollection = firestore.collection(FirestoreCollections.ENCOUNTERS)

    /**
     * Encounter を Firestore の ENCOUNTERS/{encounterId} に保存する。
     *
     * userA/userB の順番ゆれを避けるため、保存時にユーザー名を並べ替える。
     * これにより taro-hana と hana-taro を同じペアとして扱える。
     */
    suspend fun saveEncounter(encounter: Encounter) {
        encountersCollection
            .document(encounter.encounterId)
            .set(encounter.toFirestoreMap())
            .awaitResult()
    }

    /**
     * 指定した2人の最新すれ違い記録を 1 件取得する。
     *
     * 検索時も保存時と同じ並び順にそろえることで、
     * userA/userB の渡し順に関係なく同じペアを検索できる。
     */
    suspend fun getLastEncounter(userA: String, userB: String): Encounter? {
        val pair = normalizedUserPair(userA, userB)
        val snapshot = encountersCollection
            .whereEqualTo(FirestoreFields.Encounter.USER_A, pair.first)
            .whereEqualTo(FirestoreFields.Encounter.USER_B, pair.second)
            .orderBy(FirestoreFields.Encounter.TIMESTAMP, Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .awaitResult()

        return snapshot.documents.firstOrNull()?.toEncounterOrNull()
    }
}

// Encounter を Firestore 保存用の Map に変換する。
// userA/userB は順番ゆれを避けるため、常に normalizedUserPair() の結果で保存する。
private fun Encounter.toFirestoreMap(): Map<String, Any> {
    val pair = normalizedUserPair(userA, userB)

    return mapOf(
        FirestoreFields.Encounter.ENCOUNTER_ID to encounterId,
        FirestoreFields.Encounter.USER_A to pair.first,
        FirestoreFields.Encounter.USER_B to pair.second,
        FirestoreFields.Encounter.TIMESTAMP to timestamp
    )
}

// Firestore の 1 document を Encounter に変換する。
// 存在しない document はすれ違い記録なしとして null を返す。
private fun DocumentSnapshot.toEncounterOrNull(): Encounter? {
    if (!exists()) {
        return null
    }

    return Encounter(
        encounterId = getString(FirestoreFields.Encounter.ENCOUNTER_ID).orEmpty(),
        userA = getString(FirestoreFields.Encounter.USER_A).orEmpty(),
        userB = getString(FirestoreFields.Encounter.USER_B).orEmpty(),
        timestamp = getLong(FirestoreFields.Encounter.TIMESTAMP) ?: 0L
    )
}

// 2人のユーザー名を常に同じ順番にそろえる。
// Firestore に保存する形と検索する形を一致させるための小さなルール。
private fun normalizedUserPair(userA: String, userB: String): Pair<String, String> {
    return if (userA <= userB) {
        userA to userB
    } else {
        userB to userA
    }
}

// Firebase の Task を suspend 関数として待てるようにする小さな変換処理。
// これにより .get().awaitResult() のように、上から順に読める非同期コードにできる。
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
