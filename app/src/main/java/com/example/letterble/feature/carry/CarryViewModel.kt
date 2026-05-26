/**
 * CarryViewModel.kt
 *
 * 運搬中の手紙一覧と詳細表示に必要な状態を管理する。
 * UI はこの ViewModel の state を描画し、Repository には直接触らない。
 */
package com.example.letterble.feature.carry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.letterble.data.repository.LetterRepository
import com.example.letterble.data.repository.UserRepository
import com.example.letterble.domain.model.Letter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 運搬中の手紙画面が表示する状態。
 */
data class CarryUiState(
    // この端末に保存されている現在ユーザー名。
    val currentUserName: String = "",
    // 現在ユーザーが運搬している未到達の手紙一覧。
    val carryingLetters: List<Letter> = emptyList(),
    // 詳細画面で表示する対象の手紙。
    val selectedLetter: Letter? = null,
    // Firestore から読み込み中かどうか。
    val isLoading: Boolean = false,
    // 詳細データを Firestore から読み込み中かどうか。
    val isDetailLoading: Boolean = false,
    // 画面に表示するエラーメッセージ。エラーがなければ null。
    val errorMessage: String? = null
)

/**
 * 運搬中の手紙一覧と詳細の状態を管理する ViewModel。
 */
class CarryViewModel(
    // 現在ユーザー名を取得するための Repository。
    private val userRepository: UserRepository,
    // 運搬中の手紙データを取得するための Repository。
    private val letterRepository: LetterRepository
) : ViewModel() {
    // ViewModel 内部で更新する運搬画面の状態。
    private val _uiState = MutableStateFlow(CarryUiState())

    // 画面側に公開する読み取り専用の状態。
    val uiState: StateFlow<CarryUiState> = _uiState.asStateFlow()

    /**
     * 現在ユーザーが運搬中の手紙一覧を読み込む。
     */
    fun loadCarryingLetters() {
        // 端末内に登録済みユーザー名がない場合、Firestore へ問い合わせずに止める。
        val currentUserName = userRepository.getCurrentUserName()
        if (currentUserName.isNullOrBlank()) {
            _uiState.value = _uiState.value.copy(
                currentUserName = "",
                carryingLetters = emptyList(),
                isLoading = false,
                errorMessage = "ユーザー登録が必要です"
            )
            return
        }

        // Firestore 読み込みは時間がかかるため coroutine で実行する。
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                currentUserName = currentUserName,
                isLoading = true,
                errorMessage = null
            )

            try {
                // USERS の carrying_letter_ids を基点に運搬中の手紙だけを取得する。
                val letters = letterRepository.getCarryingLetters(currentUserName)
                _uiState.value = _uiState.value.copy(
                    carryingLetters = letters,
                    isLoading = false
                )
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    carryingLetters = emptyList(),
                    isLoading = false,
                    errorMessage = exception.message ?: "運搬中の手紙を取得できませんでした"
                )
            }
        }
    }

    /**
     * 指定された手紙IDの詳細データを読み込む。
     */
    fun loadLetterDetail(letterId: String) {
        if (letterId.isBlank()) {
            _uiState.value = _uiState.value.copy(
                selectedLetter = null,
                isDetailLoading = false,
                errorMessage = "手紙IDが指定されていません"
            )
            return
        }

        // 一覧にある手紙なら先に state へ反映し、詳細画面の初期表示を早くする。
        val cachedLetter = _uiState.value.carryingLetters.firstOrNull { letter ->
            letter.letterId == letterId
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                selectedLetter = cachedLetter,
                isDetailLoading = true,
                errorMessage = null
            )

            try {
                // 詳細画面は letterId を入口にするため、一覧未経由でも Repository から取得する。
                val letter = letterRepository.getLetter(letterId)
                _uiState.value = _uiState.value.copy(
                    selectedLetter = letter,
                    isDetailLoading = false,
                    errorMessage = if (letter == null) "手紙が見つかりませんでした" else null
                )
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    selectedLetter = null,
                    isDetailLoading = false,
                    errorMessage = exception.message ?: "手紙の詳細を取得できませんでした"
                )
            }
        }
    }
}

/**
 * CarryViewModel を作るための Factory。
 */
class CarryViewModelFactory(
    // AppContainer で組み立て済みの UserRepository。
    private val userRepository: UserRepository,
    // AppContainer で組み立て済みの LetterRepository。
    private val letterRepository: LetterRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CarryViewModel::class.java)) {
            return CarryViewModel(
                userRepository = userRepository,
                letterRepository = letterRepository
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
