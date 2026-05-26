/**
 * ReceivedScreen.kt
 *
 * 役割:
 * - 受信した手紙の一覧を表示する
 * - 手紙選択イベントを画面遷移へつなぐ
 */
package com.example.letterble.feature.received

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.letterble.di.AppContainer
import com.example.letterble.ui.components.CommonButton

/**
 * 受信した手紙一覧画面。
 *
 * 画面表示用の状態はReceivedViewModelから受け取り、
 * このComposableではRepositoryやDataSourceを直接呼ばない。
 * そうすることで、AGENT.mdの UI -> ViewModel -> Repository の流れを保つ。
 */
@Composable
fun ReceivedScreen(
    appContainer: AppContainer,
    onLetterClicked: (String) -> Unit,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    /**
     * AppContainerからRepositoryを受け取り、Factory経由でViewModelを作る。
     *
     * ReceivedViewModel()を直接呼ぶ書き方もできるが、Composeのライフサイクルに乗らない。
     * viewModel(factory = ...)にすると、画面回転や再描画でも同じViewModelを再利用しやすい。
     */
    val viewModel: ReceivedViewModel = viewModel(
        factory = ReceivedViewModelFactory(
            userRepository = appContainer.userRepository,
            letterRepository = appContainer.letterRepository,
            locationRepository = appContainer.locationRepository
        )
    )

    /**
     * StateFlowをComposeで読めるStateに変換する。
     *
     * uiState.valueを直接読むだけだと、状態が変わってもComposeが再描画しづらい。
     * collectAsState()を使うことで、loadingや一覧が変わったとき画面が自然に更新される。
     */
    val uiState by viewModel.uiState.collectAsState()

    /**
     * 画面が表示されたタイミングで一覧を読み込む。
     *
     * ViewModelのinitで自動読み込みする書き方もできるが、
     * 同じViewModelを詳細画面でも使うため、initに置くと詳細画面でも一覧読み込みが走ってしまう。
     * そのため、一覧画面に必要な読み込みはReceivedScreen側から明示的に呼ぶ。
     */
    LaunchedEffect(viewModel) {
        viewModel.loadReceivedLetters()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "受信した手紙",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = if (uiState.currentUserName.isBlank()) {
                "ユーザー未登録"
            } else {
                "宛先: ${uiState.currentUserName}"
            },
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        val errorMessage = uiState.errorMessage

        when {
            uiState.isLoading -> {
                ReceivedLoadingContent()
            }

            errorMessage != null -> {
                ReceivedErrorContent(
                    errorMessage = errorMessage,
                    onRetryClicked = viewModel::loadReceivedLetters
                )
            }

            uiState.isEmpty -> {
                ReceivedEmptyContent()
            }

            else -> {
                ReceivedLetterList(
                    letters = uiState.receivedLetters,
                    onLetterClicked = onLetterClicked
                )
            }
        }

        CommonButton(
            text = "戻る",
            modifier = Modifier.padding(top = 24.dp),
            onClick = onBackClicked
        )
    }
}

/**
 * 読み込み中の表示。
 *
 * 画面本体のwhenの中に直接書くこともできるが、
 * 状態ごとのUIを小さいComposableへ分けると、一覧・空・エラーの差が読みやすい。
 */
@Composable
private fun ReceivedLoadingContent() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Text(
            modifier = Modifier.padding(top = 12.dp),
            text = "読み込み中",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

/**
 * 受信した手紙が1件もないときの表示。
 */
@Composable
private fun ReceivedEmptyContent() {
    Text(
        modifier = Modifier.fillMaxWidth(),
        text = "届いた手紙はありません",
        style = MaterialTheme.typography.bodyMedium
    )
}

/**
 * 読み込み失敗時の表示。
 *
 * 再試行ボタンはViewModelのloadReceivedLetters()をもう一度呼ぶ。
 * 画面側は「再試行された」ことだけを伝え、取得処理の中身はViewModelに任せる。
 */
@Composable
private fun ReceivedErrorContent(
    errorMessage: String,
    onRetryClicked: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = errorMessage,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium
        )
        CommonButton(
            text = "再試行",
            modifier = Modifier.padding(top = 12.dp),
            onClick = onRetryClicked
        )
    }
}

/**
 * 受信した手紙の一覧。
 *
 * Columnで全件並べることもできるが、件数が増えたときに重くなる。
 * LazyColumnは表示されている行を中心に描画するため、一覧画面に向いている。
 */
@Composable
private fun ReceivedLetterList(
    letters: List<ReceivedLetterListItem>,
    onLetterClicked: (String) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = letters,
            key = { letter -> letter.letterId }
        ) { letter ->
            ReceivedLetterRow(
                letter = letter,
                onClick = { onLetterClicked(letter.letterId) }
            )
        }
    }
}

/**
 * 一覧の1行。
 *
 * Card全体をclickableにして、行のどこを押しても詳細へ進めるようにしている。
 * Buttonを行ごとに置く書き方もできるが、一覧では行タップの方が自然で、画面もすっきりする。
 */
@Composable
private fun ReceivedLetterRow(
    letter: ReceivedLetterListItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "From: ${letter.fromUser}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (letter.isDelivered) "到着済み" else "配送中",
                    style = MaterialTheme.typography.labelMedium
                )
            }

            Text(
                modifier = Modifier.padding(top = 6.dp),
                text = letter.sentencePreview.ifBlank { "本文なし" },
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                modifier = Modifier.padding(top = 6.dp),
                text = "To: ${letter.toUser}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
