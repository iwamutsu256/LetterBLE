package com.example.letterble.di

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.letterble.data.datasource.firestore.UserFirestoreDataSource
import com.example.letterble.data.datasource.local.DraftLocalDataSource
import com.example.letterble.data.datasource.local.UserLocalDataSource
import com.example.letterble.data.repository.DraftRepository
import com.example.letterble.data.repository.UserRepository
import com.example.letterble.feature.edit_letter.EditLetterViewModel

/**
 * App-wide dependency entry point.
 *
 * Keep dependencies wired here with manual constructor injection until the
 * object graph becomes large enough to justify Hilt.
 */
interface AppContainer {
    // ユーザー登録と現在ユーザー名の参照で共通利用する Repository。
    val userRepository: UserRepository

    fun editLetterViewModelFactory(): ViewModelProvider.Factory
}

class DefaultAppContainer(
    context: Context
) : AppContainer {
    // SharedPreferences 用 DataSource は ApplicationContext から一度だけ作る。
    private val applicationContext = context.applicationContext

    // Firestore 用 DataSource も AppContainer 側でまとめて生成する。
    private val userFirestoreDataSource = UserFirestoreDataSource()

    // 現在ユーザー名用のローカル DataSource を AppContainer で管理する。
    private val userLocalDataSource = UserLocalDataSource(applicationContext)

    // 下書き用のローカル DataSource も同じ AppContainer で管理する。
    private val draftLocalDataSource = DraftLocalDataSource(applicationContext)

    override val userRepository: UserRepository = UserRepository(
        userLocalDataSource = userLocalDataSource,
        userFirestoreDataSource = userFirestoreDataSource
    )

    private val draftRepository = DraftRepository(draftLocalDataSource)

    /**
     * 手紙作成画面に必要な依存関係を渡して ViewModel を生成する。
     */
    override fun editLetterViewModelFactory(): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return EditLetterViewModel(draftRepository) as T
            }
        }
    }
}
