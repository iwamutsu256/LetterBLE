/**
 * ReceivedViewModel.kt
 *
 * 役割:
 * - 受信した手紙の取得
 * - 詳細データ取得
 * - tree生成
 *
 * 呼び出し:
 * - LetterRepository
 * - LocationRepository
 * - BuildRouteTreeUseCase
 */
package com.example.letterble.feature.received

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
 * 受信一覧の1行に表示するためのデータ。
 *
 * Letterをそのまま画面に渡す書き方もできるが、一覧画面では本文全文やtree全体はまだ不要。
 * そのため、一覧で必要な項目だけを切り出した専用の型にしている。
 *
 * この形にすると、あとでReceivedScreenを作るときに
 * 「一覧に必要な情報」と「詳細画面に必要な情報」を分けて考えやすい。
 */
data class ReceivedLetterListItem(
    val letterId: String,
    val fromUser: String,
    val toUser: String,
    val sentencePreview: String,
    val isDelivered: Boolean
)

/**
 * ReceivedScreenが見る状態を1つにまとめたもの。
 *
 * 画面側で複数の変数を別々に持つ書き方もできるが、
 * Composeでは「画面に必要な状態を1つのdata classにまとめる」形にすると、
 * 状態の流れが追いやすく、再描画も扱いやすい。
 */
data class ReceivedUiState(
    val currentUserName: String = "",
    val receivedLetters: List<ReceivedLetterListItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    /**
     * 空表示を出すべきかどうかをViewModel側で判定するための値。
     *
     * 画面側で毎回
     * receivedLetters.isEmpty() && !isLoading && errorMessage == null
     * と書くこともできるが、条件が増えるとUIが読みにくくなる。
     * そのため、状態に近い判断はUiStateの中に置いている。
     */
    val isEmpty: Boolean
        get() = !isLoading && errorMessage == null && receivedLetters.isEmpty()
}

class ReceivedViewModel(
    /**
     * 現在ログイン中のユーザー名を取るためのRepository。
     *
     * UserLocalDataSourceを直接呼ぶ書き方もできるが、
     * AGENT.mdのルールではViewModelからDataSourceを直接呼ばない。
     * ViewModel -> Repository -> DataSource の流れにしておくことで、
     * 保存先がSharedPreferences以外に変わってもViewModelを変えずに済む。
     */
    private val userRepository: UserRepository,
    /**
     * 受信した手紙を取るためのRepository。
     *
     * Firestoreのwhere条件をViewModelに直接書くこともできるが、
     * ViewModelは画面状態の管理に寄せたいので、取得条件はRepository/DataSource側に任せる。
     */
    private val letterRepository: LetterRepository
) : ViewModel() {
    /**
     * ViewModelの中だけで更新する可変のStateFlow。
     *
     * MutableStateFlowを外に公開すると、画面側から状態を書き換えられてしまう。
     * そのため、外には下のuiStateだけを公開し、更新はViewModel内に閉じ込める。
     */
    private val _uiState = MutableStateFlow(ReceivedUiState())

    /**
     * 画面が読み取るためのStateFlow。
     *
     * LiveDataでも実装できるが、このプロジェクトではCoroutineとComposeを使うため、
     * KotlinのFlowとして扱えるStateFlowにしている。
     */
    val uiState: StateFlow<ReceivedUiState> = _uiState.asStateFlow()

    init {
        // 画面を開いたときに一度読み込む。手動更新が必要になったら同じ関数を再利用できる。
        loadReceivedLetters()
    }

    /**
     * 現在ユーザー宛てに届いた手紙を読み込む。
     *
     * 手順:
     * 1. ローカルに保存済みの現在ユーザー名を取得する
     * 2. ユーザー名がなければエラー状態にする
     * 3. LetterRepository.getReceivedLetters(userName)で受信済み手紙だけ取得する
     * 4. 画面一覧用のReceivedLetterListItemに変換してstateへ入れる
     */
    fun loadReceivedLetters() {
        // 登録タスクで保存した現在ユーザー名を使う。Firestoreから再取得しないので画面表示が軽い。
        val currentUserName = userRepository.getCurrentUserName().orEmpty()

        if (currentUserName.isBlank()) {
            // ユーザー名がない状態ではFirestore検索条件を作れないので、ここで止める。
            _uiState.value = _uiState.value.copy(
                currentUserName = "",
                receivedLetters = emptyList(),
                isLoading = false,
                errorMessage = "ユーザー名が登録されていません"
            )
            return
        }

        // Firestoreアクセスは時間がかかるため、メインスレッドを止めないようcoroutineで実行する。
        viewModelScope.launch {
            // 読み込み開始時点でloadingをtrueにし、前回のエラーは消す。
            _uiState.value = _uiState.value.copy(
                currentUserName = currentUserName,
                isLoading = true,
                errorMessage = null
            )

            try {
                // Repository側で「to_user == currentUserName かつ is_survival == false」に絞り込む。
                // ViewModelではFirestoreの列名や検索条件を知らないまま、画面に必要な形へ変換する。
                val receivedLetters = letterRepository
                    .getReceivedLetters(currentUserName)
                    .map { letter -> letter.toReceivedLetterListItem() }

                // copy()を使うと、変更したい項目だけを書ける。
                // ReceivedUiState(...)を毎回全部書くより、既存状態を保ったまま安全に更新しやすい。
                _uiState.value = _uiState.value.copy(
                    currentUserName = currentUserName,
                    receivedLetters = receivedLetters,
                    isLoading = false,
                    errorMessage = null
                )
            } catch (exception: Exception) {
                // 失敗時もクラッシュさせず、画面が表示できるerrorMessageに変換する。
                _uiState.value = _uiState.value.copy(
                    currentUserName = currentUserName,
                    receivedLetters = emptyList(),
                    isLoading = false,
                    errorMessage = exception.message ?: "受信した手紙の読み込みに失敗しました"
                )
            }
        }
    }
}

/**
 * ReceivedViewModelを作るためのFactory。
 *
 * 引数なしのViewModelならFactoryなしでも作れるが、
 * このViewModelはUserRepositoryとLetterRepositoryが必要。
 * Hiltをまだ使わない方針なので、手動DIとしてFactoryから渡している。
 */
class ReceivedViewModelFactory(
    private val userRepository: UserRepository,
    private val letterRepository: LetterRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReceivedViewModel::class.java)) {
            return ReceivedViewModel(
                userRepository = userRepository,
                letterRepository = letterRepository
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

/**
 * Firestoreから取得したLetterを、一覧画面に必要な形へ変換する。
 *
 * mapの中に直接変換処理を書くこともできるが、関数に分けると
 * 「Letter -> 一覧表示用データ」という意図が名前で読める。
 */
private fun Letter.toReceivedLetterListItem(): ReceivedLetterListItem {
    return ReceivedLetterListItem(
        letterId = letterId,
        fromUser = fromUser,
        toUser = toUser,
        // 一覧では本文全文ではなく先頭だけを表示する想定。全文は詳細画面の#57以降で扱う。
        sentencePreview = sentence.take(RECEIVED_SENTENCE_PREVIEW_LENGTH),
        // 受信一覧はisSurvival=falseのものだけだが、画面側で状態表示しやすい名前に変えておく。
        isDelivered = !isSurvival
    )
}

// プレビュー文字数を直書きにしないための定数。あとでUIに合わせて調整しやすい。
private const val RECEIVED_SENTENCE_PREVIEW_LENGTH = 40
