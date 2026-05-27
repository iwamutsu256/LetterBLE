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
import com.example.letterble.data.repository.DraftRepository
import com.example.letterble.data.repository.LetterRepository
import com.example.letterble.data.repository.LocationRepository
import com.example.letterble.data.repository.UserRepository
import com.example.letterble.domain.model.Edge
import com.example.letterble.domain.model.Letter
import com.example.letterble.domain.model.Location
import com.example.letterble.domain.model.Node
import com.example.letterble.domain.model.Tree
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

// 画面に表示する状態をまとめたデータクラス。
data class EditLetterUiState(
    val toName: String = "",
    val sentence: String = "",
    val isSubmitting: Boolean = false,
    val isSubmitted: Boolean = false,
    val isDraftSaved: Boolean = false,
    // 戻る確認ダイアログの表示フラグ。
    val showBackConfirmDialog: Boolean = false
)

class EditLetterViewModel(
    private val letterRepository: LetterRepository,
    private val locationRepository: LocationRepository,
    private val draftRepository: DraftRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditLetterUiState())
    val uiState: StateFlow<EditLetterUiState> = _uiState.asStateFlow()

    init {
        // 画面を開いたとき、保存済みの宛先と本文を読み込んで state に反映する。
        _uiState.value = _uiState.value.copy(
            toName = draftRepository.loadDraftToName(),
            sentence = draftRepository.loadDraftSentence()
        )
    }

    // 宛先入力欄が変わったときに state を更新する。
    fun onToChanged(value: String) {
        _uiState.value = _uiState.value.copy(toName = value)
    }

    // 本文入力欄が変わったときに state を更新する。
    fun onSentenceChanged(value: String) {
        _uiState.value = _uiState.value.copy(sentence = value)
    }

    // 下書き保存ボタンが押されたとき、宛先と本文を SharedPreferences に保存する。
    fun onSaveDraftClicked() {
        draftRepository.saveDraft(
            toName = _uiState.value.toName,
            sentence = _uiState.value.sentence
        )
        _uiState.value = _uiState.value.copy(isDraftSaved = true)
    }

    fun onDiscardAndBack() {
        draftRepository.clearDraft()
        _uiState.value = _uiState.value.copy(showBackConfirmDialog = false)
    }

    // 戻るボタンが押されたとき、確認ダイアログを表示する。
    fun onBackButtonClicked() {
        _uiState.value = _uiState.value.copy(showBackConfirmDialog = true)
    }

    // ダイアログを閉じる。
    fun onBackDialogDismissed() {
        _uiState.value = _uiState.value.copy(showBackConfirmDialog = false)
    }

    // 投函ボタンが押されたときのメイン処理。
    fun onSubmitClicked() {
        val fromUser = userRepository.getCurrentUserName() ?: return
        val state = _uiState.value

        // #54: ポスト選択が未実装のため、東京駅付近の仮座標を使う。
        val dummyLat = 35.681236
        val dummyLng = 139.767125

        val letterId = UUID.randomUUID().toString()

        // #52: 差出人を root node とした初期 tree を組み立てる。
        val rootNode = Node(
            id = fromUser,
            userName = fromUser,
            latitude = dummyLat,
            longitude = dummyLng
        )
        val initialTree = Tree(nodes = listOf(rootNode), edges = emptyList<Edge>())

        val letter = Letter(
            letterId = letterId,
            toUser = state.toName,
            fromUser = fromUser,
            sentence = state.sentence,
            isSurvival = true,
            tree = initialTree
        )

        val location = Location(
            locationId = UUID.randomUUID().toString(),
            letterId = letterId,
            userName = fromUser,
            latitude = dummyLat,
            longitude = dummyLng,
            timestamp = System.currentTimeMillis()
        )

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true)
            try {
                letterRepository.sendLetter(letter)
                locationRepository.saveLocation(location)
                draftRepository.clearDraft()
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    isSubmitted = true,
                    sentence = ""
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSubmitting = false)
            }
        }
    }
}