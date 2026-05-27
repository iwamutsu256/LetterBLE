/**
 * PostSelectViewModel.kt
 *
 * 役割:
 * - 現在地を取得する
 * - 現在地周辺のポスト候補を読み込む
 * - 選択中のポストを画面状態として管理する
 */
package com.example.letterble.feature.edit_letter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.letterble.data.datasource.local.DraftLetter
import com.example.letterble.data.datasource.location.CurrentLocationDataSource
import com.example.letterble.data.repository.DraftRepository
import com.example.letterble.data.repository.PostRepository
import com.example.letterble.data.repository.UserRepository
import com.example.letterble.domain.model.Post
import com.example.letterble.domain.usecase.SubmitLetterCommand
import com.example.letterble.domain.usecase.SubmitLetterUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ポスト選択画面の表示状態。
 */
data class PostSelectUiState(
    val posts: List<Post> = emptyList(),
    val selectedPost: Post? = null,
    val showConfirmDialog: Boolean = false,
    val isSubmitting: Boolean = false,
    val isLoading: Boolean = false,
    val message: String? = null
)

/**
 * ポスト選択画面から発生する一度きりのイベント。
 */
sealed interface PostSelectEvent {
    /** 投函完了後にホームへ戻る。 */
    data object NavigateHome : PostSelectEvent
}

/**
 * ポスト候補の取得と選択状態を管理するViewModel。
 */
class PostSelectViewModel(
    private val currentLocationDataSource: CurrentLocationDataSource,
    private val postRepository: PostRepository,
    private val draftRepository: DraftRepository,
    private val userRepository: UserRepository,
    private val submitLetterUseCase: SubmitLetterUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(PostSelectUiState())
    val uiState: StateFlow<PostSelectUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<PostSelectEvent>()
    val events: SharedFlow<PostSelectEvent> = _events.asSharedFlow()

    /**
     * 現在地から1km以内のポストを読み込む。
     */
    fun loadNearbyPosts() {
        if (_uiState.value.isLoading) {
            return
        }

        _uiState.update {
            it.copy(
                isLoading = true,
                message = null
            )
        }

        viewModelScope.launch {
            val currentLocation = currentLocationDataSource.getCurrentLocation()
            if (currentLocation == null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        message = "現在地を取得できませんでした"
                    )
                }
                return@launch
            }

            runCatching {
                postRepository.getNearbyPosts(
                    latitude = currentLocation.latitude,
                    longitude = currentLocation.longitude
                )
            }.onSuccess { posts ->
                _uiState.update {
                    it.copy(
                        posts = posts,
                        selectedPost = posts.firstOrNull(),
                        isLoading = false,
                        message = if (posts.isEmpty()) "1km以内にポストが見つかりませんでした" else null
                    )
                }
            }.onFailure {
                _uiState.update {
                    it.copy(
                        posts = emptyList(),
                        selectedPost = null,
                        isLoading = false,
                        message = "ポスト候補の取得に失敗しました"
                    )
                }
            }
        }
    }

    /**
     * ユーザーが選んだポストを状態へ反映する。
     */
    fun onPostSelected(post: Post) {
        _uiState.update {
            it.copy(
                selectedPost = post,
                showConfirmDialog = true
            )
        }
    }

    /**
     * 位置情報権限が許可されなかったことを画面状態へ反映する。
     */
    fun onLocationPermissionDenied() {
        _uiState.update {
            it.copy(
                isLoading = false,
                message = "近くのポスト検索には位置情報の許可が必要です"
            )
        }
    }

    /**
     * 確認ダイアログを閉じる。
     */
    fun onConfirmDialogDismissed() {
        _uiState.update {
            it.copy(showConfirmDialog = false)
        }
    }

    /**
     * 選択ポストの座標を投函処理へ渡す。
     */
    fun onSubmitConfirmed() {
        val selectedPost = _uiState.value.selectedPost
        if (selectedPost == null || _uiState.value.isSubmitting) {
            return
        }

        val draft = draftRepository.loadDraft()
        if (!draft.hasRequiredInput()) {
            _uiState.update {
                it.copy(
                    showConfirmDialog = false,
                    message = "投函する手紙の下書きがありません"
                )
            }
            return
        }

        val fromUser = userRepository.getCurrentUserName()
        if (fromUser.isNullOrBlank()) {
            _uiState.update {
                it.copy(
                    showConfirmDialog = false,
                    message = "ユーザー登録後に投函できます"
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                isSubmitting = true,
                message = null
            )
        }

        viewModelScope.launch {
            runCatching {
                submitLetterUseCase.execute(
                    SubmitLetterCommand(
                        fromUser = fromUser,
                        toUser = draft.toUser,
                        sentence = draft.sentence,
                        latitude = selectedPost.latitude,
                        longitude = selectedPost.longitude
                    )
                )
            }.onSuccess {
                // 投函後は端末に本文を残さないため、下書きを削除してから画面を戻す。
                draftRepository.clearDraft()
                _uiState.update {
                    it.copy(
                        showConfirmDialog = false,
                        isSubmitting = false,
                        message = "投函しました"
                    )
                }
                _events.emit(PostSelectEvent.NavigateHome)
            }.onFailure {
                _uiState.update {
                    it.copy(
                        showConfirmDialog = false,
                        isSubmitting = false,
                        message = "投函に失敗しました"
                    )
                }
            }
        }
    }

    private fun DraftLetter.hasRequiredInput(): Boolean {
        return toUser.isNotBlank() && sentence.isNotBlank()
    }
}
