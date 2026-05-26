/**
 * Firestore の LETTERS コレクションを直接読み書きする DataSource。
 *
 * Letter モデルと Firestore の Map 変換、手紙の保存・取得・受信一覧取得・
 * 運搬中手紙取得・到達状態更新を担当する。
 */
package com.example.letterble.data.datasource.firestore

import com.example.letterble.domain.model.Edge
import com.example.letterble.domain.model.Letter
import com.example.letterble.domain.model.Node
import com.example.letterble.domain.model.Tree
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * DataSource for direct access to the LETTERS collection.
 */
class LetterFirestoreDataSource(
    // テスト時に差し替えられるよう、Firestore 本体はコンストラクタで受け取る。
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    // 手紙本体を保存・取得するための LETTERS コレクション参照。
    private val lettersCollection = firestore.collection(FirestoreCollections.LETTERS)

    // 運搬中の手紙ID一覧は USERS 側にあるため、この DataSource でも参照する。
    private val usersCollection = firestore.collection(FirestoreCollections.USERS)

    /**
     * Letter を Firestore の LETTERS/{letterId} に保存する。
     *
     * Firestore は data class をそのままではなく Map として扱うため、
     * 保存前に toFirestoreMap() で Firestore 用のフィールド名へ変換する。
     */
    suspend fun saveLetter(letter: Letter) {
        lettersCollection
            .document(letter.letterId)
            .set(letter.toFirestoreMap())
            .awaitResult()
    }

    /**
     * letterId に一致する手紙を 1 件取得する。
     *
     * ドキュメントが存在しない場合は null を返し、存在する場合だけ Letter に戻す。
     */
    suspend fun getLetter(letterId: String): Letter? {
        val document = lettersCollection
            .document(letterId)
            .get()
            .awaitResult()

        return document.toLetterOrNull()
    }

    /**
     * 自分宛てに到達済みの手紙一覧を取得する。
     *
     * to_user が自分で、is_survival が false の手紙を「受信済み」として扱う。
     */
    suspend fun getReceivedLetters(userName: String): List<Letter> {
        val snapshot = lettersCollection
            .whereEqualTo(FirestoreFields.Letter.TO_USER, userName)
            .whereEqualTo(FirestoreFields.Letter.IS_SURVIVAL, false)
            .get()
            .awaitResult()

        return snapshot.toLetters()
    }

    /**
     * 指定ユーザーが運搬中の、まだ到達していない手紙一覧を取得する。
     *
     * USERS/{userName}.carrying_letter_ids から手紙IDを読み、
     * そのIDごとに LETTERS を取得して isSurvival == true のものだけ返す。
     */
    suspend fun getCarriedLetters(userName: String): List<Letter> {
        val userDocument = usersCollection
            .document(userName)
            .get()
            .awaitResult()

        val carryingLetterIds = userDocument
            .get(FirestoreFields.User.CARRYING_LETTER_IDS)
            as? List<*>

        return carryingLetterIds
            ?.filterIsInstance<String>()
            ?.mapNotNull { letterId -> getLetter(letterId) }
            ?.filter { letter -> letter.isSurvival }
            ?: emptyList()
    }

    /**
     * 手紙がまだ拡散中かどうかを更新する。
     *
     * 宛先に届いたときは isSurvival を false にすることで、以後の拡散対象から外せる。
     */
    suspend fun updateSurvival(letterId: String, isSurvival: Boolean) {
        lettersCollection
            .document(letterId)
            .update(FirestoreFields.Letter.IS_SURVIVAL, isSurvival)
            .awaitResult()
    }
}

// Letter を Firestore 保存用の Map に変換する。
// Kotlin 側は camelCase、Firestore 側は snake_case のフィールド名を使うためここで揃える。
private fun Letter.toFirestoreMap(): Map<String, Any> {
    return mapOf(
        FirestoreFields.Letter.LETTER_ID to letterId,
        FirestoreFields.Letter.TO_USER to toUser,
        FirestoreFields.Letter.FROM_USER to fromUser,
        FirestoreFields.Letter.SENTENCE to sentence,
        FirestoreFields.Letter.IS_SURVIVAL to isSurvival,
        FirestoreFields.Letter.TREE to tree.toFirestoreMap()
    )
}

// Tree は Firestore 上では nodes と edges を持つ Map として保存する。
private fun Tree.toFirestoreMap(): Map<String, Any> {
    return mapOf(
        FirestoreFields.Tree.NODES to nodes.map { node -> node.toFirestoreMap() },
        FirestoreFields.Tree.EDGES to edges.map { edge -> edge.toFirestoreMap() }
    )
}

// Node は経路上の1点を表すため、ユーザー名と緯度経度を保存する。
private fun Node.toFirestoreMap(): Map<String, Any> {
    return mapOf(
        FirestoreFields.Node.ID to id,
        FirestoreFields.Node.USER_NAME to userName,
        FirestoreFields.Node.LATITUDE to latitude,
        FirestoreFields.Node.LONGITUDE to longitude
    )
}

// Edge は Tree 内の Node 同士のつながりを表す。
private fun Edge.toFirestoreMap(): Map<String, Any> {
    return mapOf(
        FirestoreFields.Edge.FROM_NODE_ID to fromNodeId,
        FirestoreFields.Edge.TO_NODE_ID to toNodeId
    )
}

// Firestore の検索結果を、画面や Repository で扱いやすい Letter のリストに変換する。
private fun QuerySnapshot.toLetters(): List<Letter> {
    return documents.mapNotNull { document -> document.toLetterOrNull() }
}

// Firestore の 1 document を Letter に変換する。
// 存在しない document は手紙なしとして null を返す。
private fun DocumentSnapshot.toLetterOrNull(): Letter? {
    if (!exists()) {
        return null
    }

    return Letter(
        letterId = getString(FirestoreFields.Letter.LETTER_ID).orEmpty(),
        toUser = getString(FirestoreFields.Letter.TO_USER).orEmpty(),
        fromUser = getString(FirestoreFields.Letter.FROM_USER).orEmpty(),
        sentence = getString(FirestoreFields.Letter.SENTENCE).orEmpty(),
        isSurvival = getBoolean(FirestoreFields.Letter.IS_SURVIVAL) ?: true,
        tree = get(FirestoreFields.Letter.TREE).toTree()
    )
}

// Firestore から取った tree フィールドを Tree data class に戻す。
// 型が想定と違う場合は、壊れたデータで落ちないよう空の Tree を返す。
private fun Any?.toTree(): Tree {
    val treeMap = this as? Map<*, *> ?: return Tree()

    return Tree(
        nodes = treeMap[FirestoreFields.Tree.NODES].toNodes(),
        edges = treeMap[FirestoreFields.Tree.EDGES].toEdges()
    )
}

// Firestore の nodes 配列を List<Node> に戻す。
private fun Any?.toNodes(): List<Node> {
    val nodeMaps = this as? List<*> ?: return emptyList()

    return nodeMaps.mapNotNull { nodeData ->
        val nodeMap = nodeData as? Map<*, *> ?: return@mapNotNull null

        Node(
            id = nodeMap[FirestoreFields.Node.ID] as? String ?: "",
            userName = nodeMap[FirestoreFields.Node.USER_NAME] as? String ?: "",
            latitude = nodeMap[FirestoreFields.Node.LATITUDE].toDoubleOrDefault(),
            longitude = nodeMap[FirestoreFields.Node.LONGITUDE].toDoubleOrDefault()
        )
    }
}

// Firestore の edges 配列を List<Edge> に戻す。
private fun Any?.toEdges(): List<Edge> {
    val edgeMaps = this as? List<*> ?: return emptyList()

    return edgeMaps.mapNotNull { edgeData ->
        val edgeMap = edgeData as? Map<*, *> ?: return@mapNotNull null

        Edge(
            fromNodeId = edgeMap[FirestoreFields.Edge.FROM_NODE_ID] as? String ?: "",
            toNodeId = edgeMap[FirestoreFields.Edge.TO_NODE_ID] as? String ?: ""
        )
    }
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
