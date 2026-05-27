/**
 * Firestore の LETTERS/{letterId}.tree フィールドを直接読み書きする DataSource。
 *
 * Tree / Node / Edge モデルと Firestore の Map/List 変換、tree の取得・更新・
 * node と edge の追加を担当する。
 */
package com.example.letterble.data.datasource.firestore

import com.example.letterble.domain.model.Edge
import com.example.letterble.domain.model.Location
import com.example.letterble.domain.model.Node
import com.example.letterble.domain.model.Tree
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * LETTERS コレクション内の tree フィールド専用の DataSource。
 *
 * 手紙本体は LetterFirestoreDataSource が扱い、このクラスは経路表示用の
 * Tree 構造だけを読み書きする。
 */
class TreeFirestoreDataSource(
    // テスト時に差し替えられるよう、Firestore 本体はコンストラクタで受け取る。
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    // tree は LETTERS/{letterId} の中のフィールドとして保存される。
    private val lettersCollection = firestore.collection(FirestoreCollections.LETTERS)

    /**
     * 指定した手紙の tree フィールドを取得する。
     *
     * tree がまだない場合や型が想定と違う場合は、空の Tree として扱う。
     */
    suspend fun getTree(letterId: String): Tree {
        val document = lettersCollection
            .document(letterId)
            .get()
            .awaitResult()

        return document.get(FirestoreFields.Letter.TREE).toTree()
    }

    /**
     * 指定した手紙の tree フィールド全体を更新する。
     *
     * Firestore には data class のままではなく、Map/List に変換して保存する。
     */
    suspend fun updateTree(letterId: String, tree: Tree) {
        lettersCollection
            .document(letterId)
            .update(FirestoreFields.Letter.TREE, tree.toFirestoreMap())
            .awaitResult()
    }

    /**
     * tree に新しい node と、親 node から新しい node への edge を追加する。
     *
     * 重複 node を追加してよいかどうかの判断は UseCase 側の責務にして、
     * この DataSource では「読んで、足して、保存する」だけに絞る。
     */
    suspend fun addNode(
        letterId: String,
        parentUser: String,
        newUser: String,
        location: Location
    ): Boolean {
        val letterDocument = lettersCollection.document(letterId)

        return firestore.runTransaction { transaction ->
            val document = transaction.get(letterDocument)
            val currentTree = document.get(FirestoreFields.Letter.TREE).toTree()

            if (currentTree.nodes.any { node -> node.userName == newUser }) {
                return@runTransaction false
            }

            val parentNode = currentTree.nodes.firstOrNull { node ->
                node.userName == parentUser
            }

            val newNode = Node(
                id = newUser,
                userName = newUser,
                latitude = location.latitude,
                longitude = location.longitude
            )

            val newEdge = Edge(
                fromNodeId = parentNode?.id ?: parentUser,
                toNodeId = newNode.id
            )

            val updatedTree = Tree(
                nodes = currentTree.nodes + newNode,
                edges = currentTree.edges + newEdge
            )

            transaction.update(
                letterDocument,
                FirestoreFields.Letter.TREE,
                updatedTree.toFirestoreMap()
            )

            true
        }.awaitResult()
    }
}

// Tree を Firestore 保存用の Map に変換する。
// Firestore 上では nodes と edges を配列として持つ Map にして保存する。
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
