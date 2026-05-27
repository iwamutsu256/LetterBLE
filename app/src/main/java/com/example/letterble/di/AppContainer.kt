package com.example.letterble.di

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.letterble.data.datasource.firestore.EncounterFirestoreDataSource
import com.example.letterble.data.datasource.firestore.LetterFirestoreDataSource
import com.example.letterble.data.datasource.firestore.LocationFirestoreDataSource
import com.example.letterble.data.datasource.firestore.TreeFirestoreDataSource
import com.example.letterble.data.datasource.firestore.UserFirestoreDataSource
import com.example.letterble.data.datasource.local.DraftLocalDataSource
import com.example.letterble.data.datasource.local.UserLocalDataSource
import com.example.letterble.data.repository.DraftRepository
import com.example.letterble.data.repository.EncounterRepository
import com.example.letterble.data.repository.LetterRepository
import com.example.letterble.data.repository.LocationRepository
import com.example.letterble.data.repository.TreeRepository
import com.example.letterble.data.repository.UserRepository
import com.example.letterble.domain.usecase.BuildRouteTreeUseCase
import com.example.letterble.feature.edit_letter.EditLetterViewModel
import com.example.letterble.feature.received.ReceivedViewModelFactory

/**
 * App-wide dependency entry point.
 *
 * Keep dependencies wired here with manual constructor injection until the
 * object graph becomes large enough to justify Hilt.
 */
interface AppContainer {
    // ユーザー登録と現在ユーザー名の参照で共通利用する Repository。
    val userRepository: UserRepository

    // 手紙データを上位層へ提供する Repository。
    val letterRepository: LetterRepository

    // 位置履歴データを上位層へ提供する Repository。
    val locationRepository: LocationRepository

    // すれ違い履歴データを上位層へ提供する Repository。
    val encounterRepository: EncounterRepository

    // 経路 Tree データを上位層へ提供する Repository。
    val treeRepository: TreeRepository

    // 保存済み Tree と Location 履歴から表示用 Tree を決める UseCase。
    val buildRouteTreeUseCase: BuildRouteTreeUseCase

    // 受信画面系の ViewModel 生成に必要な依存関係を AppContainer 側でまとめる。
    val receivedViewModelFactory: ReceivedViewModelFactory

    // 手紙作成画面の ViewModel 生成に必要な依存関係を AppContainer 側でまとめる。
    fun editLetterViewModelFactory(): ViewModelProvider.Factory
}

class DefaultAppContainer(
    context: Context
) : AppContainer {
    // SharedPreferences 用 DataSource は ApplicationContext から一度だけ作る。
    private val applicationContext = context.applicationContext

    // Firestore 用 DataSource も AppContainer 側でまとめて生成する。
    private val userFirestoreDataSource = UserFirestoreDataSource()
    private val letterFirestoreDataSource = LetterFirestoreDataSource()
    private val locationFirestoreDataSource = LocationFirestoreDataSource()
    private val encounterFirestoreDataSource = EncounterFirestoreDataSource()
    private val treeFirestoreDataSource = TreeFirestoreDataSource()

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
    override val letterRepository = LetterRepository(letterFirestoreDataSource)
    override val locationRepository = LocationRepository(locationFirestoreDataSource)
    override val encounterRepository = EncounterRepository(encounterFirestoreDataSource)
    override val treeRepository = TreeRepository(treeFirestoreDataSource)
    override val buildRouteTreeUseCase = BuildRouteTreeUseCase()
    override val receivedViewModelFactory = ReceivedViewModelFactory(
        userRepository = userRepository,
        letterRepository = letterRepository,
        locationRepository = locationRepository,
        buildRouteTreeUseCase = buildRouteTreeUseCase
    )
}
