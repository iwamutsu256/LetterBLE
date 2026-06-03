/**
 * ReceivedDetailScreen.kt
 *
 * 役割:
 * - 受信した手紙の詳細を表示する
 * - 経路ツリー表示へつなぐ
 */
package com.example.letterble.feature.received

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.letterble.R
import com.example.letterble.di.AppContainer
import com.example.letterble.domain.model.Letter
import com.example.letterble.domain.model.Tree
import com.example.letterble.ui.components.CommonBackButton
import com.example.letterble.ui.components.CommonButton
import com.example.letterble.ui.components.LetterPaper
import com.example.letterble.ui.theme.LetterBLEColors
import com.example.letterble.ui.theme.LetterBLEFontFamilies
import com.example.letterble.ui.theme.LetterBLEFontSize
import com.example.letterble.ui.theme.LetterBLETheme
import com.example.letterble.ui.theme.NotoSansJpFontFamily

/**
 * 受信した手紙の詳細画面。
 *
 * letterIdはAppNavGraphのルート引数から受け取る。
 * 画面はそのIDをViewModelへ渡し、本文や経路概要の取得はViewModelに任せる。
 */
@Composable
fun ReceivedDetailScreen(
    appContainer: AppContainer,
    letterId: String,
    onBackClicked: () -> Unit,
    onMapClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: ReceivedViewModel = viewModel(factory = appContainer.receivedViewModelFactory)

    val uiState by viewModel.uiState.collectAsState()
    val detailState = uiState.detailState

    LaunchedEffect(letterId) {
        viewModel.loadLetterDetail(letterId)
    }

    Scaffold { innerPadding ->
        ReceivedDetailScreenContent(
            detailState = detailState,
            letterId = letterId,
            onBackClicked = onBackClicked,
            onMapClicked = onMapClicked,
            onRetryClicked = { viewModel.loadLetterDetail(letterId) },
            innerPadding = innerPadding,
            modifier = modifier
        )
    }
}

/**
 * 表示ロジックを分離したコンテンツ部分。
 */
@Composable
private fun ReceivedDetailScreenContent(
    detailState: ReceivedDetailUiState,
    letterId: String,
    onBackClicked: () -> Unit,
    onMapClicked: () -> Unit,
    onRetryClicked: () -> Unit,
    innerPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        // 背景の装飾画像 (黄色の円とオレンジの円の組み合わせ)
        Image(
            painter = painterResource(id = R.drawable.img01),
            contentDescription = null,
            modifier = Modifier
                .size(240.dp)
                .align(Alignment.TopEnd)
                .offset(x = (-40).dp, y = (-100).dp)
        )
        Image(
            painter = painterResource(id = R.drawable.img02),
            contentDescription = null,
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.TopEnd)
                .offset(x = 80.dp, y = (-30).dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // タイトル (背景画像の下に配置)
            Text(
                text = "手紙とルート",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = LetterBLEFontFamilies.NotoSansJp,
                    fontWeight = FontWeight.Black,
                    fontSize = LetterBLEFontSize.Headline,
                    color = LetterBLEColors.TextPrimary
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 64.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            when {
                detailState.isLoading -> {
                    ReceivedDetailLoadingContent()
                }

                detailState.errorMessage != null -> {
                    ReceivedDetailErrorContent(
                        errorMessage = detailState.errorMessage,
                        onRetryClicked = onRetryClicked
                    )
                }

                detailState.detail != null -> {
                    ReceivedDetailContent(
                        detail = detailState.detail,
                        onMapClicked = onMapClicked
                    )
                }

                else -> {
                    Text(
                        text = "手紙の詳細を読み込んでいます",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // 戻るボタン (PostSelectScreenと同じ位置)
        CommonBackButton(
            modifier = Modifier.padding(top = innerPadding.calculateTopPadding()),
            onClick = onBackClicked
        )
    }
}

@Preview(showSystemUi = true)
@Composable
private fun ReceivedDetailScreenSystemUIPreview() {
    LetterBLETheme {
        Scaffold { innerPadding ->
            ReceivedDetailScreenContent(
                detailState = ReceivedDetailUiState(
                    detail = ReceivedLetterDetail(
                        letter = Letter(
                            letterId = "123",
                            fromUser = "Alice",
                            toUser = "Bob",
                            sentence = "Hello world!"
                        ),
                        locations = emptyList(),
                        tree = Tree(),
                        routeSummary = "2 users"
                    )
                ),
                letterId = "123",
                onBackClicked = {},
                onMapClicked = {},
                onRetryClicked = {},
                innerPadding = innerPadding
            )
        }
    }
}

/**
 * 詳細読み込み中の表示。
 */
@Composable
private fun ReceivedDetailLoadingContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp),
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
 * 詳細読み込み失敗時の表示。
 */
@Composable
private fun ReceivedDetailErrorContent(
    errorMessage: String,
    onRetryClicked: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = errorMessage,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        CommonButton(
            text = "再試行",
            modifier = Modifier.padding(top = 12.dp),
            onClick = onRetryClicked
        )
    }
}

/**
 * 読み込み済みの手紙詳細。
 *
 * 本文と経路地図を表示する。
 */
@Composable
private fun ReceivedDetailContent(
    detail: ReceivedLetterDetail,
    onMapClicked: () -> Unit
) {
    val letter = detail.letter

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 手紙本体 (EditLetterScreenと同様の見た目、ただし読み取り専用)
        LetterPaper {
            Column(modifier = Modifier.fillMaxWidth()) {
                // 宛先
                Text(
                    text = letter.toUser.ifBlank { "宛先なし" },
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = NotoSansJpFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = LetterBLEColors.TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 本文
                Text(
                    text = letter.sentence.ifBlank { "本文なし" },
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = NotoSansJpFontFamily,
                        color = LetterBLEColors.TextPrimary,
                        lineHeight = 28.sp
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 差出人 (右下に配置)
                Text(
                    text = letter.fromUser.ifBlank { "不明" },
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = NotoSansJpFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = LetterBLEColors.TextPrimary
                    ),
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }

        // 経路地図
        RouteMapScreen(
            letter = letter,
            tree = detail.tree,
            onMapClicked = onMapClicked,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}
