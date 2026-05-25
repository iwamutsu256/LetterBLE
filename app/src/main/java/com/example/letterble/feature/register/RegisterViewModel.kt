/**
 * RegisterViewModel.kt
 *
 * 登録画面の状態と処理を管理するファイル。
 * 画面側はこの ViewModel の uiState を見て表示を変える。
 */

// このファイルが登録画面 feature の置き場所にあることを示す。
package com.example.letterble.feature.register

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
// 画面状態を変更できる StateFlow。
import kotlinx.coroutines.flow.MutableStateFlow
// 画面側に公開する読み取り専用の StateFlow。
import kotlinx.coroutines.flow.StateFlow
// MutableStateFlow を読み取り専用として公開するために使う。
import kotlinx.coroutines.flow.asStateFlow
// Firestore 保存などの suspend 関数を呼ぶために使う。
import kotlinx.coroutines.launch

/**
 * 登録画面が表示する状態をまとめたデータ。
 */
data class RegisterUiState(
    // 入力欄に表示するユーザー名。
    val userName: String = "",
    // Firestore 保存中かどうか。
    val isLoading: Boolean = false,
    // 登録済みとして Home に進んでよいかどうか。
    val isRegistered: Boolean = false,
    // 画面に表示するエラーメッセージ。エラーがなければ null。
    val errorMessage: String? = null
)

/**
 * 登録画面の状態と登録処理を管理する ViewModel。
 */
class RegisterViewModel(
    // ユーザー保存や現在ユーザー名の取得を行う Repository。
    private val userRepository: UserRepository
) : ViewModel() {
    // ViewModel 内部で更新する登録画面の状態。
    private val _uiState = MutableStateFlow(RegisterUiState())

    // 画面側に公開する読み取り専用の状態。
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    // ViewModel が作られた直後に、すでに登録済みか確認する。
    init {
        // 端末内に保存済みユーザー名があるか調べる。
        checkRegisteredUser()
    }

    /**
     * ユーザー名入力欄の文字が変わったときに呼ばれる。
     */
    fun onNameChanged(userName: String) {
        // 入力されたユーザー名を状態に反映し、古いエラー表示は消す。
        _uiState.value = _uiState.value.copy(
            // 入力欄の表示値を更新する。
            userName = userName,
            // 入力し直したら前のエラーは不要なので消す。
            errorMessage = null
        )
    }

    /**
     * 開始ボタンが押されたときに呼ばれる。
     */
    fun onNameSubmitClicked() {
        // 入力欄の前後の空白を取り除いたユーザー名を使う。
        val userName = _uiState.value.userName.trim()

        // ユーザー名が空なら保存せず、エラーを表示する。
        if (userName.isEmpty()) {
            // 画面に入力エラーを表示する。
            _uiState.value = _uiState.value.copy(
                // ユーザーに入力が必要だと伝える。
                errorMessage = "ユーザー名を入力してください"
            )
            // ここで処理を止める。
            return
        }

        // Firestore 保存は時間がかかるので coroutine で実行する。
        viewModelScope.launch {
            // 保存中として画面にローディングを出せる状態にする。
            _uiState.value = _uiState.value.copy(
                // 保存中フラグを立てる。
                isLoading = true,
                // 保存を再試行するので古いエラーは消す。
                errorMessage = null
            )

            // Firestore 保存は失敗する可能性があるので try-catch で囲む。
            try {
                // Firestore の USERS コレクションにユーザーを保存する。
                userRepository.saveUser(userName)

                // 次回起動時に登録画面をスキップできるよう、端末内にもユーザー名を保存する。
                userRepository.saveCurrentUserName(userName)

                // 保存に成功したので、登録済み状態にする。
                _uiState.value = _uiState.value.copy(
                    // trim 済みのユーザー名を状態に入れ直す。
                    userName = userName,
                    // 保存が終わったのでローディングを止める。
                    isLoading = false,
                    // true になると RegisterScreen が Home への遷移を呼ぶ。
                    isRegistered = true
                )
            } catch (exception: Exception) {
                // Firestore 保存などが失敗した場合、画面にエラーを表示する。
                _uiState.value = _uiState.value.copy(
                    // 失敗したのでローディングを止める。
                    isLoading = false,
                    // Firebase からの詳しいエラーがあれば表示し、なければ汎用メッセージを表示する。
                    errorMessage = exception.message ?: "ユーザー登録に失敗しました"
                )
            }
        }
    }

    /**
     * 端末内に保存済みユーザー名があるか確認する。
     */
    private fun checkRegisteredUser() {
        // SharedPreferences に保存済みの現在ユーザー名を取得する。
        val currentUserName = userRepository.getCurrentUserName()

        // 保存済みユーザー名があれば、登録済みとして扱う。
        if (currentUserName != null) {
            // 保存済みユーザー名を状態に反映し、Home へ進める状態にする。
            _uiState.value = _uiState.value.copy(
                // Home でも使えるようユーザー名を状態に入れる。
                userName = currentUserName,
                // true になると RegisterScreen が Home への遷移を呼ぶ。
                isRegistered = true
            )
        }
    }
}

/**
 * RegisterViewModel を作るための Factory。
 *
 * RegisterViewModel は UserRepository が必要なので、
 * Compose の viewModel() にこの Factory を渡して作る。
 */
class RegisterViewModelFactory(
    // UserLocalDataSource を作るために Context を受け取る。
    private val context: Context
) : ViewModelProvider.Factory {
    // ViewModelProvider.Factory の create 関数を実装する。
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // 作ろうとしている ViewModel が RegisterViewModel か確認する。
        if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
            // 端末内保存用 DataSource を作る。
            val userLocalDataSource = UserLocalDataSource(context.applicationContext)

            // UserRepository を作る。
            val userRepository = UserRepository(
                // 現在ユーザー名の保存・取得に使う DataSource を渡す。
                userLocalDataSource = userLocalDataSource
            )

            // Repository を渡して RegisterViewModel を作る。
            return RegisterViewModel(userRepository) as T
        }

        // RegisterViewModel 以外を作ろうとした場合はエラーにする。
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
