/**
 * HomeViewModel.kt
 *
 * 役割:
 * - ホーム画面のボタンイベントを受け取る
 * - 画面遷移イベントを発行する
 *
 * 注意:
 * - NavControllerを持たない
 * - 遷移先の実行はUI層に任せる
 */
package com.example.letterble.feature.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.letterble.data.datasource.local.UserLocalDataSource
import com.example.letterble.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val currentUserName: String = ""
)

/**
 * ホーム画面のUIイベントを画面遷移イベントへ変換するViewModel。
 */
class HomeViewModel(
    private val userRepository: UserRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())

    /**
     * UI層が購読するホーム画面の状態。
     */
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _navigationEvents = MutableSharedFlow<HomeNavigationEvent>()

    /**
     * UI層が購読する画面遷移イベント。
     */
    val navigationEvents: SharedFlow<HomeNavigationEvent> = _navigationEvents.asSharedFlow()

    init {
        loadCurrentUserName()
    }

    /**
     * ローカルに保存されている現在ユーザー名を読み込む。
     */
    private fun loadCurrentUserName() {
        _uiState.value = _uiState.value.copy(
            currentUserName = userRepository.getCurrentUserName().orEmpty()
        )
    }

    /**
     * 受信した手紙のボタンが押されたときに呼ばれる。
     */
    fun onReceivedClicked() {
        emitNavigation(HomeNavigationEvent.NavigateToReceived)
    }

    /**
     * 運搬中の手紙のボタンが押されたときに呼ばれる。
     */
    fun onCarryClicked() {
        emitNavigation(HomeNavigationEvent.NavigateToCarry)
    }

    /**
     * 手紙作成ボタンが押されたときに呼ばれる。
     */
    fun onCreateLetterClicked() {
        emitNavigation(HomeNavigationEvent.NavigateToEditLetter)
    }

    /**
     * SharedFlowへ画面遷移イベントを送信する。
     */
    private fun emitNavigation(event: HomeNavigationEvent) {
        viewModelScope.launch {
            _navigationEvents.emit(event)
        }
    }
}

class HomeViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            val userLocalDataSource = UserLocalDataSource(context.applicationContext)
            val userRepository = UserRepository(
                userLocalDataSource = userLocalDataSource
            )

            return HomeViewModel(userRepository) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

/**
 * ホーム画面から発生する画面遷移イベント。
 */
sealed interface HomeNavigationEvent {
    /** 受信一覧画面へ遷移する。 */
    data object NavigateToReceived : HomeNavigationEvent

    /** 運搬中一覧画面へ遷移する。 */
    data object NavigateToCarry : HomeNavigationEvent

    /** 手紙作成画面へ遷移する。 */
    data object NavigateToEditLetter : HomeNavigationEvent
}