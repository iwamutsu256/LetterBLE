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
import com.example.letterble.data.datasource.location.CurrentLocationDataSource
import com.example.letterble.data.repository.PostRepository
import com.example.letterble.domain.model.Post
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
    val isLoading: Boolean = false,
    val message: String? = null
)

/**
 * ポスト候補の取得と選択状態を管理するViewModel。
 */
class PostSelectViewModel(
    private val currentLocationDataSource: CurrentLocationDataSource,
    private val postRepository: PostRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(PostSelectUiState())
    val uiState: StateFlow<PostSelectUiState> = _uiState.asStateFlow()

    init {
        loadNearbyPosts()
    }

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
     * 確認ダイアログを閉じる。
     */
    fun onConfirmDialogDismissed() {
        _uiState.update {
            it.copy(showConfirmDialog = false)
        }
    }
}
