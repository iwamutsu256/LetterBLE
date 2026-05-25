/**
 * HomeViewModel.kt
 *
 * ホーム画面の状態とボタン操作を管理するファイル。
 * NavController は持たず、画面遷移したいことだけをイベントとして外に知らせる。
 */

// このファイルがホーム画面 feature の置き場所にあることを示す。
package com.example.letterble.feature.home

// ViewModelFactory で UserLocalDataSource を作るために Context を使う。
import android.content.Context
// 画面の状態管理をする ViewModel の基底クラス。
import androidx.lifecycle.ViewModel
// 引数が必要な ViewModel を作るための Factory。
import androidx.lifecycle.ViewModelProvider
// ViewModel の中で coroutine を起動するために使う。
import androidx.lifecycle.viewModelScope
// 現在ユーザー名を端末内に保存する DataSource。
import com.example.letterble.data.datasource.local.UserLocalDataSource
// ユーザー情報の保存・取得をまとめて扱う Repository。
import com.example.letterble.data.repository.UserRepository
// 一回きりの画面遷移イベントを流すために使う。
import kotlinx.coroutines.flow.MutableSharedFlow
// 画面状態を変更できる StateFlow。
import kotlinx.coroutines.flow.MutableStateFlow
// 画面側に公開する読み取り専用の SharedFlow。
import kotlinx.coroutines.flow.SharedFlow
// 画面側に公開する読み取り専用の StateFlow。
import kotlinx.coroutines.flow.StateFlow
// MutableSharedFlow を読み取り専用として公開するために使う。
import kotlinx.coroutines.flow.asSharedFlow
// MutableStateFlow を読み取り専用として公開するために使う。
import kotlinx.coroutines.flow.asStateFlow
// navigationEvents を emit するために coroutine を使う。
import kotlinx.coroutines.launch

/**
 * ホーム画面が表示する状態をまとめたデータ。
 */
data class HomeUiState(
    // ホーム左上に表示する現在ユーザー名。
    val currentUserName: String = ""
)

/**
 * ホーム画面の状態と画面遷移イベントを管理する ViewModel。
 */
class HomeViewModel(
    // 現在ユーザー名を取得するための Repository。
    private val userRepository: UserRepository
) : ViewModel() {
    // ViewModel 内部で更新するホーム画面の状態。
    private val _uiState = MutableStateFlow(HomeUiState())

    // 画面側に公開する読み取り専用の状態。
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // ViewModel 内部から送信する画面遷移イベント。
    private val _navigationEvents = MutableSharedFlow<HomeNavigationEvent>()

    // 画面側に公開する読み取り専用の画面遷移イベント。
    val navigationEvents: SharedFlow<HomeNavigationEvent> = _navigationEvents.asSharedFlow()

    // ViewModel が作られた直後に、現在ユーザー名を読み込む。
    init {
        // 端末内に保存されているユーザー名を uiState に反映する。
        loadCurrentUserName()
    }

    /**
     * 端末内に保存されている現在ユーザー名を読み込む。
     */
    private fun loadCurrentUserName() {
        // Repository から現在ユーザー名を取得して、画面状態に入れる。
        _uiState.value = _uiState.value.copy(
            // 保存されていない場合は空文字にする。
            currentUserName = userRepository.getCurrentUserName().orEmpty()
        )
    }

    /**
     * 「受信した手紙」ボタンが押されたときに呼ばれる。
     */
    fun onReceivedClicked() {
        // 受信一覧画面へ進みたいことを画面側に知らせる。
        emitNavigation(HomeNavigationEvent.NavigateToReceived)
    }

    /**
     * 「運搬中の手紙」ボタンが押されたときに呼ばれる。
     */
    fun onCarryClicked() {
        // 運搬中一覧画面へ進みたいことを画面側に知らせる。
        emitNavigation(HomeNavigationEvent.NavigateToCarry)
    }

    /**
     * 「手紙を書く」ボタンが押されたときに呼ばれる。
     */
    fun onCreateLetterClicked() {
        // 手紙作成画面へ進みたいことを画面側に知らせる。
        emitNavigation(HomeNavigationEvent.NavigateToEditLetter)
    }

    /**
     * 画面遷移イベントを SharedFlow に流す。
     */
    private fun emitNavigation(event: HomeNavigationEvent) {
        // emit は suspend 関数なので coroutine で実行する。
        viewModelScope.launch {
            // 画面側が collect している navigationEvents にイベントを流す。
            _navigationEvents.emit(event)
        }
    }
}

/**
 * HomeViewModel を作るための Factory。
 *
 * HomeViewModel は UserRepository が必要なので、
 * Compose の viewModel() にこの Factory を渡して作る。
 */
class HomeViewModelFactory(
    // UserLocalDataSource を作るために Context を受け取る。
    private val context: Context
) : ViewModelProvider.Factory {
    // ViewModelProvider.Factory の create 関数を実装する。
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // 作ろうとしている ViewModel が HomeViewModel か確認する。
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            // 端末内保存用 DataSource を作る。
            val userLocalDataSource = UserLocalDataSource(context.applicationContext)

            // UserRepository を作る。
            val userRepository = UserRepository(
                // 現在ユーザー名の保存・取得に使う DataSource を渡す。
                userLocalDataSource = userLocalDataSource
            )

            // Repository を渡して HomeViewModel を作る。
            return HomeViewModel(userRepository) as T
        }

        // HomeViewModel 以外を作ろうとした場合はエラーにする。
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

/**
 * HomeViewModel から HomeScreen に送る画面遷移イベント。
 */
sealed interface HomeNavigationEvent {
    // 受信一覧画面へ遷移したいイベント。
    data object NavigateToReceived : HomeNavigationEvent

    // 運搬中一覧画面へ遷移したいイベント。
    data object NavigateToCarry : HomeNavigationEvent

    // 手紙作成画面へ遷移したいイベント。
    data object NavigateToEditLetter : HomeNavigationEvent
}
