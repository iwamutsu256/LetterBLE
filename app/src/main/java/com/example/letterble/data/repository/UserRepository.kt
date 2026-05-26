/**
 * UserRepository.kt
 *
 * ユーザー情報をアプリ側から使いやすい形で提供するファイル。
 * Firestore 用 DataSource とローカル保存用 DataSource をまとめて扱う。
 */

// このファイルが Repository の置き場所にあることを示す。
package com.example.letterble.data.repository

// Firestore の USERS コレクションとやりとりする DataSource。
import com.example.letterble.data.datasource.firestore.UserFirestoreDataSource
// 端末内に現在ユーザー名を保存する DataSource。
import com.example.letterble.data.datasource.local.UserLocalDataSource
// アプリ内で使うユーザーデータの型。
import com.example.letterble.domain.model.User

/**
 * ユーザー情報を扱う Repository。
 *
 * ViewModel は DataSource を直接触らず、この Repository を通してユーザー情報を扱う。
 */
class UserRepository(
    // 現在ユーザー名を端末内に保存・取得するための DataSource。
    private val userLocalDataSource: UserLocalDataSource,
    // ユーザー情報を Firestore に保存・取得するための DataSource。
    private val userFirestoreDataSource: UserFirestoreDataSource = UserFirestoreDataSource()
) {
    /**
     * 新しいユーザーを Firestore に保存する。
     *
     * ここでは Firestore 保存だけを行う。
     * 端末内への現在ユーザー名保存は saveCurrentUserName() で別に行う。
     */
    suspend fun saveUser(userName: String) {
        // Firestore に保存する User オブジェクトを作る。
        val user = User(
            // 入力されたユーザー名を User に入れる。
            userName = userName,
            // 登録直後はまだ運んでいる手紙がないので空リストにする。
            carryingLetterIds = emptyList()
        )

        // Firestore 用 DataSource に保存処理を任せる。
        userFirestoreDataSource.saveUser(user)
    }

    /**
     * 指定されたユーザー名の User を Firestore から取得する。
     *
     * 見つからない場合は null が返る。
     */
    suspend fun getUser(userName: String): User? {
        // Firestore 用 DataSource に取得処理を任せる。
        return userFirestoreDataSource.getUser(userName)
    }

    /**
     * この端末で現在使うユーザー名を保存する。
     *
     * 次回起動時に登録画面をスキップするために使う。
     */
    fun saveCurrentUserName(userName: String) {
        // ローカル保存用 DataSource に保存処理を任せる。
        userLocalDataSource.saveCurrentUserName(userName)
    }

    /**
     * この端末に保存されている現在ユーザー名を取得する。
     *
     * 未登録なら null が返る。
     */
    fun getCurrentUserName(): String? {
        // ローカル保存用 DataSource に取得処理を任せる。
        return userLocalDataSource.getCurrentUserName()
    }

    /**
     * この端末に保存されている現在ユーザー名を削除する。
     *
     * ログアウトや登録やり直しを実装するときに使う。
     */
    fun clearCurrentUserName() {
        // ローカル保存用 DataSource に削除処理を任せる。
        userLocalDataSource.clearCurrentUserName()
    }
}
