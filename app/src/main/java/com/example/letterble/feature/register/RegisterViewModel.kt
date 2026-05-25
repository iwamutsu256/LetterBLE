/**
 * RegisterViewModel.kt
 *
 * 役割:
 * - ユーザー入力の状態管理
 * - 登録処理の制御
 * - BLE開始トリガー
 *
 * 呼び出し:
 * - UserRepository
 * - BleRepository
 */

// TODO: userName状態をStateとして保持する
// TODO: onNameChangedでstateを更新する
// TODO: onNameSubmitClickedでUserRepository.saveUser()を呼ぶ
// TODO: 権限処理結果を受け取る関数を実装する
// TODO: 権限許可後にBleRepository.startBle()を呼ぶ
// TODO: 完了後にホーム画面遷移イベントをemitする

package com.example.letterble.feature.register

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.letterble.data.datasource.local.UserLocalDataSource
import com.example.letterble.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RegisterUiState(
    val userName: String = "",
    val isLoading: Boolean = false,
    val isRegistered: Boolean = false,
    val errorMessage: String? = null
)

class RegisterViewModel(
    private val userRepository: UserRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    init {
        checkRegisteredUser()
    }

    fun onNameChanged(userName: String) {
        _uiState.value = _uiState.value.copy(
            userName = userName,
            errorMessage = null
        )
    }

    fun onNameSubmitClicked() {
        val userName = _uiState.value.userName.trim()

        if (userName.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "ユーザー名を入力してください"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            try {
                userRepository.saveUser(userName)
                userRepository.saveCurrentUserName(userName)

                _uiState.value = _uiState.value.copy(
                    userName = userName,
                    isLoading = false,
                    isRegistered = true
                )
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = exception.message ?: "ユーザー登録に失敗しました"
                )
            }
        }
    }

    private fun checkRegisteredUser() {
        val currentUserName = userRepository.getCurrentUserName()

        if (currentUserName != null) {
            _uiState.value = _uiState.value.copy(
                userName = currentUserName,
                isRegistered = true
            )
        }
    }
}

class RegisterViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
            val userLocalDataSource = UserLocalDataSource(context.applicationContext)
            val userRepository = UserRepository(
                userLocalDataSource = userLocalDataSource
            )

            return RegisterViewModel(userRepository) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}