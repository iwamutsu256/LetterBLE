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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * ホーム画面のUIイベントを画面遷移イベントへ変換するViewModel。
 */
class HomeViewModel : ViewModel() {
    private val _navigationEvents = MutableSharedFlow<HomeNavigationEvent>()

    /**
     * UI層が購読する画面遷移イベント。
     */
    val navigationEvents: SharedFlow<HomeNavigationEvent> = _navigationEvents.asSharedFlow()

    /**
     * 受信した手紙ボタンが押されたときに呼ばれる。
     */
    fun onReceivedClicked() {
        emitNavigation(HomeNavigationEvent.NavigateToReceived)
    }

    /**
     * 運搬中の手紙ボタンが押されたときに呼ばれる。
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
