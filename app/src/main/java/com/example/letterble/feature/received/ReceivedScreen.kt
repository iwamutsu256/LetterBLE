/**
 * ReceivedScreen.kt
 *
 * 役割:
 * - 受信した手紙の一覧を表示する
 * - 手紙選択イベントを画面遷移へつなぐ
 */
package com.example.letterble.feature.received

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.letterble.di.AppContainer
import com.example.letterble.ui.components.CommonBottomNavigation
import com.example.letterble.ui.components.CommonBackButton
import com.example.letterble.ui.components.CommonButton
import com.example.letterble.R
import com.example.letterble.ui.theme.LetterBLEColors
import com.example.letterble.ui.theme.LetterBLEFontFamilies
import com.example.letterble.ui.theme.LetterBLETheme

/**
 * 受信した手紙一覧画面。
 *
 * 画面表示用の状態はReceivedViewModelから受け取り、
 * このComposableではRepositoryやDataSourceを直接呼ばない。
 * そうすることで、AGENT.mdの UI -> ViewModel -> Repository の流れを保つ。
 */
@Composable
fun ReceivedScreen(
    navController: NavHostController,
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
    val viewModel: ReceivedViewModel = viewModel(factory = appContainer.receivedViewModelFactory)

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

    BackHandler {
        onBackClicked()
    }

    CommonBottomNavigation(navController = navController) { innerPadding ->
        ReceivedScreenContent(
            uiState = uiState,
            onLetterClicked = onLetterClicked,
            onBackClicked = onBackClicked,
            onRetryClicked = viewModel::loadReceivedLetters,
            innerPadding = innerPadding,
            modifier = modifier
        )
    }
}

/**
 * 表示ロジックを分離したコンテンツ部分。プレビューで使用可能。
 */
@Composable
private fun ReceivedScreenContent(
    uiState: ReceivedUiState,
    onLetterClicked: (String) -> Unit,
    onBackClicked: () -> Unit,
    onRetryClicked: () -> Unit,
    innerPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = innerPadding.calculateBottomPadding())
            .background(LetterBLEColors.AppBackground)
    ) {
        ReceivedBackgroundImages()

        CommonBackButton(onClick = onBackClicked)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ReceivedMessagesContent(
                uiState = uiState,
                onLetterClicked = onLetterClicked,
                onRetryClicked = onRetryClicked
            )
        }
    }
}

@Composable
private fun ReceivedBackgroundImages() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.img01),
            contentDescription = null,
            modifier = Modifier
                .size(320.dp)
                .offset(x = (-100).dp, y = (-400).dp)
        )
        Image(
            painter = painterResource(id = R.drawable.img02),
            contentDescription = null,
            modifier = Modifier
                .size(120.dp)
                .offset(x = 40.dp, y = (-360).dp)
        )
    }
}

/**
 * 読み込み中の表示。
 *
 * 画面本体のwhenの中に直接書くこともできるが、
 * 状態ごとのUIを小さいComposableへ分けると、一覧・空・エラーの差が読みやすい。
 */
@Preview
@Composable
private fun ReceivedLoadingContent() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            color = LetterBLEColors.Accent
        )
        Text(
            modifier = Modifier.padding(top = 12.dp),
            text = "読み込み中",
            color = LetterBLEColors.TextPrimary,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

/**
 * 受信した手紙が1件もないときの表示。
 */
@Preview
@Composable
private fun ReceivedEmptyContent() {
    Text(
        modifier = Modifier.fillMaxWidth(),
        text = "届いた手紙はありません",
        color = LetterBLEColors.TextPrimary,
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center
    )
}

/**
 * 読み込み失敗時の表示。
 *
 * 再試行ボタンはViewModelのloadReceivedLetters()をもう一度呼ぶ。
 * 画面側は「再試行された」ことだけを伝え、取得処理の中身はViewModelに任せる。
 */
@Composable
fun ReceivedErrorContent(
    errorMessage: String,
    onRetryClicked: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = errorMessage,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium
        )
        CommonButton(
            text = "再試行",
            modifier = Modifier
                .width(200.dp)
                .padding(top = 12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = LetterBLEColors.TextPrimary,
                contentColor = LetterBLEColors.AppBackground
            ),
            onClick = onRetryClicked
        )
    }
}
@Preview
@Composable
fun ReceivedErrorContentPreview(){
    ReceivedErrorContent(
        errorMessage = "ネットワークエラー",
        onRetryClicked = {}
    )
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
    onLetterClicked: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 100.dp),
        modifier = modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalArrangement = Arrangement.spacedBy(30.dp)
    ) {
        items(
            count = letters.size,
            key = { index -> letters[index].letterId }
        ) { index ->

            val letter = letters[index]

            val imageRes = if (!letter.isDelivered) {
                R.drawable.letter01
            } else {
                R.drawable.letter02
            }
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = null,
                modifier = Modifier
                    .size(75.dp)
                    .clickable { onLetterClicked(letter.letterId) }
            )
        }
    }
}

@Composable
private fun ReceivedMessagesContent(
    uiState: ReceivedUiState,
    onLetterClicked: (String) -> Unit,
    onRetryClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(700.dp)
            .padding(25.dp),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "受け取ったメッセージ",
            color = LetterBLEColors.TextPrimary,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 50.dp)
        )

        val errorMessage = uiState.errorMessage
        when {
            uiState.isLoading -> {
                ReceivedLoadingContent()
            }

            errorMessage != null -> {
                ReceivedErrorContent(
                    errorMessage = errorMessage,
                    onRetryClicked = onRetryClicked
                )
            }

            uiState.isEmpty -> {
                ReceivedEmptyContent()
            }

            else -> {
                ReceivedLetterList(
                    letters = uiState.receivedLetters,
                    onLetterClicked = onLetterClicked,
                    modifier = Modifier.height(280.dp)
                )
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun ReceivedScreenSystemUIPreview() {
    val navController = androidx.navigation.compose.rememberNavController()
    LetterBLETheme {
        CommonBottomNavigation(navController = navController) { innerPadding ->
            ReceivedScreenContent(
                uiState = ReceivedUiState(
                    currentUserName = "sample-user",
                    receivedLetters = listOf(
                        ReceivedLetterListItem("1", "Alice", "sample-user", "Hello!", false)
                    )
                ),
                onLetterClicked = {},
                onBackClicked = {},
                onRetryClicked = {},
                innerPadding = innerPadding
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
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = LetterBLEFontFamilies.PostNoBillsColombo
                    ),
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
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = LetterBLEFontFamilies.PostNoBillsColombo
                )
            )
        }
    }
}
