/**
 * UserRepository.kt
 *
 * 役割:
 * - ユーザー情報の取得/保存
 *
 * 使用Datasource:
 * - Firestore
 */

// TODO: 各DataSourceを受け取る
// TODO: DataSourceの関数を呼び出すラッパーを作る
// TODO: 上位層に返すデータの整形を行う（必要なら）
// TODO: ビジネスロジックを書かない
package com.example.letterble.data.repository

import com.example.letterble.data.datasource.firestore.UserFirestoreDataSource
import com.example.letterble.data.datasource.local.UserLocalDataSource
import com.example.letterble.domain.model.User

class UserRepository(
    private val userLocalDataSource: UserLocalDataSource,
    private val userFirestoreDataSource: UserFirestoreDataSource = UserFirestoreDataSource()
) {
    suspend fun saveUser(userName: String) {
        val user = User(
            userName = userName,
            carryingLetterIds = emptyList()
        )

        userFirestoreDataSource.saveUser(user)
    }

    suspend fun getUser(userName: String): User? {
        return userFirestoreDataSource.getUser(userName)
    }

    fun saveCurrentUserName(userName: String) {
        userLocalDataSource.saveCurrentUserName(userName)
    }

    fun getCurrentUserName(): String? {
        return userLocalDataSource.getCurrentUserName()
    }

    fun clearCurrentUserName() {
        userLocalDataSource.clearCurrentUserName()
    }
}