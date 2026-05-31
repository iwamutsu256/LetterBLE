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
    val currentLatitude: Double? = null,
    val currentLongitude: Double? = null,
    val showConfirmDialog: Boolean = false,
    val isSubmitting: Boolean = false,
    val isSubmitted: Boolean = false,
    val isLoading: Boolean = false,
    val isPostSearchLoading: Boolean = false,
    val errorMessage: String? = null,
    val canRetryPostSearch: Boolean = false,
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
        if (_uiState.value.isLoading || _uiState.value.isPostSearchLoading) {
            return
        }

        _uiState.update {
            val hasCurrentPosition = it.currentLatitude != null && it.currentLongitude != null
            it.copy(
                isLoading = !hasCurrentPosition,
                isPostSearchLoading = hasCurrentPosition,
                errorMessage = null,
                canRetryPostSearch = false,
                message = null
            )
        }

        viewModelScope.launch {
            val currentLocation = currentLocationDataSource.getCurrentLocation()
            if (currentLocation == null) {
                _uiState.update {
                    it.copy(
                        posts = emptyList(),
                        selectedPost = null,
                        currentLatitude = null,
                        currentLongitude = null,
                        isLoading = false,
                        isPostSearchLoading = false,
                        errorMessage = "現在地を取得できませんでした",
                        canRetryPostSearch = true,
                        message = null
                    )
                }
                return@launch
            }

            _uiState.update {
                it.copy(
                    posts = emptyList(),
                    selectedPost = null,
                    currentLatitude = currentLocation.latitude,
                    currentLongitude = currentLocation.longitude,
                    isLoading = false,
                    isPostSearchLoading = true,
                    errorMessage = null,
                    canRetryPostSearch = false,
                    message = null
                )
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
                        selectedPost = null,
                        currentLatitude = currentLocation.latitude,
                        currentLongitude = currentLocation.longitude,
                        isLoading = false,
                        isPostSearchLoading = false,
                        errorMessage = null,
                        canRetryPostSearch = false,
                        message = if (posts.isEmpty()) "1km以内にポストが見つかりませんでした" else null
                    )
                }
            }.onFailure {
                _uiState.update {
                    it.copy(
                        posts = emptyList(),
                        selectedPost = null,
                        isLoading = false,
                        isPostSearchLoading = false,
                        errorMessage = "ポスト候補の取得に失敗しました",
                        canRetryPostSearch = true,
                        message = null
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
                selectedPost = post
            )
        }
    }

    /**
     * 地図上のピン以外が押されたら選択を解除する。
     */
    fun onMapClicked() {
        _uiState.update {
            it.copy(selectedPost = null)
        }
    }

    /**
     * 投函ボタンが押された際に確認ダイアログを表示する。
     */
    fun onPostSubmitClicked() {
        if (_uiState.value.selectedPost != null) {
            _uiState.update { it.copy(showConfirmDialog = true) }
        }
    }

    /**
     * 位置情報権限が許可されなかったことを画面状態へ反映する。
     */
    fun onLocationPermissionDenied() {
        _uiState.update {
            it.copy(
                posts = emptyList(),
                selectedPost = null,
                currentLatitude = null,
                currentLongitude = null,
                isLoading = false,
                isPostSearchLoading = false,
                errorMessage = "1km以内のポスト検索には正確な位置情報の許可が必要です",
                canRetryPostSearch = false,
                message = null
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
                    errorMessage = "投函する手紙の下書きがありません",
                    canRetryPostSearch = false,
                    message = null
                )
            }
            return
        }

        val fromUser = userRepository.getCurrentUserName()
        if (fromUser.isNullOrBlank()) {
            _uiState.update {
                it.copy(
                    showConfirmDialog = false,
                    errorMessage = "ユーザー登録後に投函できます",
                    canRetryPostSearch = false,
                    message = null
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                showConfirmDialog = false,
                isSubmitting = true,
                isSubmitted = true,
                errorMessage = null,
                canRetryPostSearch = false,
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
                        isSubmitting = false,
                        errorMessage = null,
                        canRetryPostSearch = false,
                        message = "投函しました"
                    )
                }
            }.onFailure {
                _uiState.update {
                    it.copy(
                        isSubmitted = false,
                        isSubmitting = false,
                        errorMessage = "投函に失敗しました",
                        canRetryPostSearch = false,
                        message = null
                    )
                }
            }
        }
    }

    /**
     * 投函完了後のOKボタンが押された際にホームへ戻る。
     */
    fun onSubmittedOkClicked() {
        viewModelScope.launch {
            _events.emit(PostSelectEvent.NavigateHome)
        }
    }

    private fun DraftLetter.hasRequiredInput(): Boolean {
        return toUser.isNotBlank() && sentence.isNotBlank()
    }
}
