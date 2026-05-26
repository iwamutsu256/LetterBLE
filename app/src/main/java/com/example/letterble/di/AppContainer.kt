package com.example.letterble.di

import android.content.Context
import com.example.letterble.data.datasource.firestore.UserFirestoreDataSource
import com.example.letterble.data.datasource.local.UserLocalDataSource
import com.example.letterble.data.repository.UserRepository

/**
 * App-wide dependency entry point.
 *
 * Keep dependencies wired here with manual constructor injection until the
 * object graph becomes large enough to justify Hilt.
 */
interface AppContainer {
    // ユーザー登録と現在ユーザー名の参照で共通利用する Repository。
    val userRepository: UserRepository
}

class DefaultAppContainer(
    context: Context
) : AppContainer {
    // SharedPreferences 用 DataSource は ApplicationContext から一度だけ作る。
    private val userLocalDataSource = UserLocalDataSource(context.applicationContext)

    // Firestore 用 DataSource も AppContainer 側でまとめて生成する。
    private val userFirestoreDataSource = UserFirestoreDataSource()

    override val userRepository: UserRepository = UserRepository(
        userLocalDataSource = userLocalDataSource,
        userFirestoreDataSource = userFirestoreDataSource
    )
}
