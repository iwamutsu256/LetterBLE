/**
 * Firestore の LOCATIONS コレクションを直接読み書きする DataSource。
 *
 * Location モデルと Firestore の Map 変換、位置履歴の保存・手紙IDでの取得・
 * ユーザー名での取得を担当する。
 */
package com.example.letterble.data.datasource.firestore

import com.example.letterble.domain.model.Location
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * LOCATIONS コレクション専用の DataSource。
 *
 * Repository や UseCase からは Firestore の細かい書き方を見せず、
 * Location data class として扱えるようにする。
 */
class LocationFirestoreDataSource(
    // テスト時に差し替えられるよう、Firestore 本体はコンストラクタで受け取る。
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    // 位置履歴を保存・取得するための LOCATIONS コレクション参照。
    private val locationsCollection = firestore.collection(FirestoreCollections.LOCATIONS)

    /**
     * Location を Firestore の LOCATIONS/{locationId} に保存する。
     *
     * 位置履歴は、手紙が投函・中継された地点を時系列で残すためのデータ。
     * Firestore 保存前に toFirestoreMap() で Firestore 用のフィールド名へ変換する。
     */
    suspend fun saveLocation(location: Location) {
        locationsCollection
            .document(location.locationId)
            .set(location.toFirestoreMap())
            .awaitResult()
    }

    /**
     * 指定した手紙IDに紐づく位置履歴を取得する。
     *
     * timestamp 昇順で返すことで、後続の Tree 生成や経路表示で古い順に扱える。
     */
    suspend fun getLocationsByLetter(letterId: String): List<Location> {
        val snapshot = locationsCollection
            .whereEqualTo(FirestoreFields.Location.LETTER_ID, letterId)
            .get()
            .awaitResult()

        // Firestoreで whereEqualTo + orderBy を同時に使うと複合インデックスが必要になることがある。
        // 詳細画面を開くだけでインデックス未作成エラーにならないよう、取得後にアプリ側で並べ替える。
        return snapshot.toLocations().sortedBy { location -> location.timestamp }
    }

    /**
     * 指定したユーザー名に紐づく位置履歴を取得する。
     *
     * 運搬中一覧や受信一覧の下準備として、ユーザーが関わった手紙IDを探すときに使う。
     * こちらも timestamp 昇順で返す。
     */
    suspend fun getLocationsByUser(userName: String): List<Location> {
        val snapshot = locationsCollection
            .whereEqualTo(FirestoreFields.Location.USER_NAME, userName)
            .get()
            .awaitResult()

        // getLocationsByLetter() と同じ理由で、並び替えはアプリ側で行う。
        return snapshot.toLocations().sortedBy { location -> location.timestamp }
    }
}

// Location を Firestore 保存用の Map に変換する。
// Kotlin 側は camelCase、Firestore 側は snake_case のフィールド名を使うためここで揃える。
private fun Location.toFirestoreMap(): Map<String, Any> {
    return mapOf(
        FirestoreFields.Location.LOCATION_ID to locationId,
        FirestoreFields.Location.LETTER_ID to letterId,
        FirestoreFields.Location.USER_NAME to userName,
        FirestoreFields.Location.LATITUDE to latitude,
        FirestoreFields.Location.LONGITUDE to longitude,
        FirestoreFields.Location.TIMESTAMP to timestamp
    )
}

// Firestore の検索結果を、画面や Repository で扱いやすい Location のリストに変換する。
private fun QuerySnapshot.toLocations(): List<Location> {
    return documents.mapNotNull { document -> document.toLocationOrNull() }
}

// Firestore の 1 document を Location に変換する。
// 存在しない document は位置履歴なしとして null を返す。
private fun DocumentSnapshot.toLocationOrNull(): Location? {
    if (!exists()) {
        return null
    }

    return Location(
        locationId = getString(FirestoreFields.Location.LOCATION_ID).orEmpty(),
        letterId = getString(FirestoreFields.Location.LETTER_ID).orEmpty(),
        userName = getString(FirestoreFields.Location.USER_NAME).orEmpty(),
        latitude = get(FirestoreFields.Location.LATITUDE).toDoubleOrDefault(),
        longitude = get(FirestoreFields.Location.LONGITUDE).toDoubleOrDefault(),
        timestamp = getLong(FirestoreFields.Location.TIMESTAMP) ?: 0L
    )
}

// Firestore から数値を読むと Integer/Long/Double などに分かれることがあるため Double に寄せる。
private fun Any?.toDoubleOrDefault(): Double {
    return when (this) {
        is Double -> this
        is Number -> toDouble()
        else -> 0.0
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
