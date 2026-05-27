/**
 * 手紙の経路 Tree をアプリ側から扱いやすい形で提供する Repository。
 *
 * Firestore の LETTERS/{letterId}.tree フィールドへの読み書きは
 * TreeFirestoreDataSource に任せる。
 */
package com.example.letterble.data.repository

import com.example.letterble.data.datasource.firestore.TreeFirestoreDataSource
import com.example.letterble.domain.model.Location
import com.example.letterble.domain.model.Tree
import com.example.letterble.domain.usecase.RelayTreeRepository

/**
 * tree フィールドを扱う Repository。
 *
 * 今の段階では DataSource の薄いラッパーとして、UseCase から使う関数名を提供する。
 */
class TreeRepository(
    private val treeFirestoreDataSource: TreeFirestoreDataSource = TreeFirestoreDataSource()
) : RelayTreeRepository {
    /**
     * 指定した手紙の Tree を取得する。
     */
    suspend fun getTree(letterId: String): Tree {
        return treeFirestoreDataSource.getTree(letterId)
    }

    /**
     * 指定した手紙の Tree 全体を更新する。
     */
    suspend fun updateTree(letterId: String, tree: Tree) {
        treeFirestoreDataSource.updateTree(letterId, tree)
    }

    /**
     * 指定した手紙の Tree に、新しい node と edge を追加する。
     *
     * 重複追加してよいかどうかの判断は UseCase 側に置き、
     * Repository は DataSource へ処理を渡すだけにする。
     */
    override suspend fun addNode(
        letterId: String,
        parentUser: String,
        newUser: String,
        location: Location
    ) {
        treeFirestoreDataSource.addNode(
            letterId = letterId,
            parentUser = parentUser,
            newUser = newUser,
            location = location
        )
    }
}
