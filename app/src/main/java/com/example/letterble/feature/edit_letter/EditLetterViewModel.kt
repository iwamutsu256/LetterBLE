/**
 * EditLetterViewModel.kt
 *
 * 役割:
 * - 手紙作成状態管理
 * - 下書き保存
 * - ポスト取得
 * - 手紙送信
 *
 * 呼び出す:
 * - DraftRepository
 * - PostRepository
 * - LetterRepository
 * - LocationRepository
 */
package com.example.letterble.feature.edit_letter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.letterble.data.datasource.local.DraftLetter
import com.example.letterble.data.repository.DraftRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 手紙作成画面の表示状態。
 */
data class EditLetterUiState(
    val toUser: String = "",
    val sentence: String = "",
    val message: String? = null,
    val isDraftSaved: Boolean = false
) {
    /**
     * 戻る時に確認が必要かどうかをUIが判断するための値。
     */
    val hasInput: Boolean
        get() = toUser.isNotBlank() || sentence.isNotBlank()
}

/**
 * 手紙作成画面から発生する一度きりのイベント。
 */
sealed interface EditLetterEvent {
    /** 投函先選択へ進む。 */
    data object NavigateToPostSelect : EditLetterEvent
}

/**
 * 手紙作成の入力状態と下書き操作を管理するViewModel。
 */
class EditLetterViewModel(
    private val draftRepository: DraftRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(EditLetterUiState())
    val uiState: StateFlow<EditLetterUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<EditLetterEvent>()
    val events: SharedFlow<EditLetterEvent> = _events.asSharedFlow()

    init {
        loadDraft()
    }

    /**
     * 宛先入力を画面状態へ反映する。
     */
    fun onToUserChanged(toUser: String) {
        _uiState.update {
            it.copy(
                toUser = toUser,
                message = null,
                isDraftSaved = false
            )
        }
    }

    /**
     * 本文入力を画面状態へ反映する。
     */
    fun onSentenceChanged(sentence: String) {
        _uiState.update {
            it.copy(
                sentence = sentence,
                message = null,
                isDraftSaved = false
            )
        }
    }

    /**
     * 現在の入力内容を下書きとして保存する。
     */
    fun onSaveDraftClicked() {
        val state = _uiState.value
        draftRepository.saveDraft(
            DraftLetter(
                toUser = state.toUser,
                sentence = state.sentence
            )
        )
        _uiState.update {
            it.copy(
                message = "下書きを保存しました",
                isDraftSaved = true
            )
        }
    }

    /**
     * 保存済み下書きを削除し、入力欄も空に戻す。
     */
    fun onClearDraftClicked() {
        draftRepository.clearDraft()
        _uiState.value = EditLetterUiState(message = "下書きを削除しました")
    }

    /**
     * 投函へ進める最低限の入力があるか確認し、次画面へのイベントを発行する。
     */
    fun onSubmitClicked() {
        val state = _uiState.value
        if (state.toUser.isBlank() || state.sentence.isBlank()) {
            _uiState.update {
                it.copy(message = "宛先と本文を入力してください")
            }
            return
        }

        viewModelScope.launch {
            _events.emit(EditLetterEvent.NavigateToPostSelect)
        }
    }

    /**
     * 起動時に1件だけ保存された下書きを読み込む。
     */
    private fun loadDraft() {
        val draft = draftRepository.loadDraft()
        _uiState.value = EditLetterUiState(
            toUser = draft.toUser,
            sentence = draft.sentence,
            isDraftSaved = draft.toUser.isNotBlank() || draft.sentence.isNotBlank()
        )
    }
}
