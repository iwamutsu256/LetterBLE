/**
 * UserFirestoreDataSource.kt
 *
 * Firestore の USERS コレクションと直接やりとりするファイル。
 * Repository や ViewModel には Firestore の細かい書き方を見せないようにする。
 */

// このファイルが Firestore 用 DataSource の置き場所にあることを示す。
package com.example.letterble.data.datasource.firestore

// アプリ内で使うユーザーデータの型。
import com.example.letterble.domain.model.User
// Firebase の非同期処理を表す型。
import com.google.android.gms.tasks.Task
// Firestore 本体を使うための型。
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
// Firebase の Task を Kotlin の suspend 関数として待つために使う。
import kotlinx.coroutines.suspendCancellableCoroutine
// 非同期処理が成功したときに、止めていた処理を再開するために使う。
import kotlin.coroutines.resume
// 非同期処理が失敗したときに、例外として処理を再開するために使う。
import kotlin.coroutines.resumeWithException

/**
 * Firestore にある USERS コレクション専用の DataSource。
 *
 * このクラスの責任は「User を Firestore に保存すること」と
 * 「Firestore から User を取り出すこと」だけ。
 */
class UserFirestoreDataSource(
    // テスト時などに差し替えられるよう、Firestore インスタンスを外から渡せる形にしている。
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    // USERS コレクションへの参照を先に作っておく。
    private val usersCollection = firestore.collection(FirestoreCollections.USERS)

    // BLE 通信用IDとユーザー名を対応付ける USER_IDS コレクションへの参照。
    private val userIdsCollection = firestore.collection(FirestoreCollections.USER_IDS)

    /**
     * User を Firestore の USERS コレクションに保存する。
     *
     * ドキュメントIDには userName を使う。
     * 例: USERS/taro
     */
    suspend fun saveUser(user: User) {
        // Firestore に保存するため、User クラスを Map に変換する。
        val data = mapOf(
            // Firestore の user_name フィールドに user.userName を入れる。
            FirestoreFields.User.USER_NAME to user.userName,
            // Firestore の carrying_letter_ids フィールドに運搬中手紙ID一覧を入れる。
            FirestoreFields.User.CARRYING_LETTER_IDS to user.carryingLetterIds
        )

        // USERS コレクションに対して保存処理を行う。
        usersCollection
            // ユーザー名をドキュメントIDとして使う。
            .document(user.userName)
            // 作った Map を Firestore に保存する。
            .set(data)
            // Firebase の非同期保存が終わるまで待つ。
            .awaitResult()
    }

    /**
     * User が未登録の場合だけ USERS に保存する。
     *
     * 既存ユーザー名でログインしたときに carrying_letter_ids を空で上書きしないため、
     * 既存ドキュメントがある場合は何もせず false を返す。
     */
    suspend fun saveUserIfAbsent(user: User): Boolean {
        val userDocument = usersCollection.document(user.userName)
        return firestore.runTransaction { transaction ->
            val snapshot = transaction.get(userDocument)
            if (snapshot.exists()) {
                false
            } else {
                transaction.set(
                    userDocument,
                    mapOf(
                        FirestoreFields.User.USER_NAME to user.userName,
                        FirestoreFields.User.CARRYING_LETTER_IDS to user.carryingLetterIds
                    )
                )
                true
            }
        }.awaitResult()
    }

    /**
     * userName に一致する User を Firestore から取得する。
     *
     * 見つからない場合は null を返す。
     */
    suspend fun getUser(userName: String): User? {
        // USERS/{userName} のドキュメントを取得する。
        val document = usersCollection
            // 探したいユーザー名のドキュメントを指定する。
            .document(userName)
            // Firestore からドキュメントを取得する。
            .get()
            // Firebase の非同期取得が終わるまで待つ。
            .awaitResult()

        // ドキュメントが存在しない場合は、ユーザーがいないので null を返す。
        if (!document.exists()) {
            return null
        }

        // carrying_letter_ids は Firestore から取ると型が曖昧なので、一度 List<*> として受け取る。
        val carryingLetterIds = document
            // carrying_letter_ids フィールドを取り出す。
            .get(FirestoreFields.User.CARRYING_LETTER_IDS)
            // リストとして扱える場合だけ List<*> にする。
            as? List<*>

        // Firestore のドキュメントを、アプリ内で使う User 型に戻して返す。
        return User(
            // user_name フィールドを String として取り出す。null の場合は空文字にする。
            userName = document.getString(FirestoreFields.User.USER_NAME).orEmpty(),
            // リストの中から String だけを取り出す。null の場合は空リストにする。
            carryingLetterIds = carryingLetterIds
                ?.filterIsInstance<String>()
                ?: emptyList()
        )
    }

    /**
     * USER_IDS から、指定したユーザー名に対応する BLE 通信用IDを取得する。
     */
    suspend fun getUserIdByUserName(userName: String): String? {
        val snapshot = userIdsCollection
            .whereEqualTo(FirestoreFields.UserId.USER_NAME, userName)
            .limit(1)
            .get()
            .awaitResult()

        return snapshot.documents
            .firstOrNull()
            ?.getString(FirestoreFields.UserId.USER_ID)
    }

    /**
     * USER_IDS/{userId} からユーザー名を取得する。
     */
    suspend fun getUserNameByUserId(userId: String): String? {
        val document = userIdsCollection
            .document(userId)
            .get()
            .awaitResult()

        return document
            .takeIf { it.exists() }
            ?.getString(FirestoreFields.UserId.USER_NAME)
    }

    /**
     * USER_IDS/{userId} が未使用の場合だけ userId と userName の対応を作る。
     */
    suspend fun saveUserIdIfAbsent(userId: String, userName: String): Boolean {
        val userIdDocument = userIdsCollection.document(userId)
        return firestore.runTransaction { transaction ->
            val snapshot = transaction.get(userIdDocument)
            if (snapshot.exists()) {
                false
            } else {
                transaction.set(
                    userIdDocument,
                    mapOf(
                        FirestoreFields.UserId.USER_ID to userId,
                        FirestoreFields.UserId.USER_NAME to userName
                    )
                )
                true
            }
        }.awaitResult()
    }

    /**
     * 指定したユーザーの運搬中手紙IDリストに、手紙IDを追加する。
     *
     * Firestore の arrayUnion を使うことで、同じIDを重複追加しない。
     */
    suspend fun addCarryingLetterIds(userName: String, letterIds: List<String>) {
        if (letterIds.isEmpty()) {
            return
        }

        usersCollection
            .document(userName)
            .update(
                FirestoreFields.User.CARRYING_LETTER_IDS,
                FieldValue.arrayUnion(*letterIds.toTypedArray())
            )
            .awaitResult()
    }
}

/**
 * Firebase の Task<T> を Kotlin の suspend 関数として待てるようにする補助関数。
 *
 * Firestore の set() や get() は Task を返すため、
 * ViewModel 側で coroutine として扱いやすいようにここで変換している。
 */
private suspend fun <T> Task<T>.awaitResult(): T {
    // Firebase の処理が完了するまで coroutine を一時停止する。
    return suspendCancellableCoroutine { continuation ->
        // Firebase の処理が成功した場合の処理。
        addOnSuccessListener { result ->
            // 成功結果を返して、止めていた coroutine を再開する。
            continuation.resume(result)
        }

        // Firebase の処理が失敗した場合の処理。
        addOnFailureListener { exception ->
            // 例外を投げる形で、止めていた coroutine を再開する。
            continuation.resumeWithException(exception)
        }
    }
}
