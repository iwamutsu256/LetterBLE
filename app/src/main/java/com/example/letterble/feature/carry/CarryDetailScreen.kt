/**
 * CarryDetailScreen.kt
 *
 * 役割:
 * - 運搬中の手紙の詳細を表示する
 * - 経路ツリー表示へつなぐ
 */
package com.example.letterble.feature.carry

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.letterble.R
import com.example.letterble.di.AppContainer
import com.example.letterble.domain.model.Tree

/**
 * 運搬中の手紙の詳細画面を表示する。
 *
 * @param appContainer Repository を ViewModel に渡すための依存関係入口
 * @param letterId 詳細表示する手紙ID
 * @param onBackClicked 前の画面へ戻るためのコールバック
 * @param modifier 画面全体に適用するModifier
 */
@Composable
fun CarryDetailScreen(
    appContainer: AppContainer,
    letterId: String,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CarryViewModel = viewModel(
        factory = CarryViewModelFactory(
            userRepository = appContainer.userRepository,
            letterRepository = appContainer.letterRepository
        )
    )
) {
    val uiState by viewModel.uiState.collectAsState()

    // 詳細画面はURL引数の letterId を入口として、必要な手紙データを読み込む。
    LaunchedEffect(letterId) {
        viewModel.loadLetterDetail(letterId)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        CarryDetailScreenContent(
            uiState = uiState,
            letterId = letterId,
            onBackClicked = onBackClicked,
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
private fun CarryDetailScreenContent(
    uiState: CarryUiState,
    letterId: String,
    onBackClicked: () -> Unit,
    onRetryClicked: () -> Unit,
    innerPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(all = 40.dp)
            .background(Color(0xFFFFFFFA))
    ){
        Image(
            painter = painterResource(id = R.drawable.img02),
            contentDescription = null,
            modifier = Modifier
                .size(200.dp)
                .offset(x = 150.dp, y = -50.dp),

        )
        Image(
            painter = painterResource(id = R.drawable.img02),
            contentDescription = null,
            modifier = Modifier
                .size(230.dp)
                .offset(x = -20.dp, y = 600.dp),

            )
        Image(
            painter = painterResource(id = R.drawable.img03),
            contentDescription = null,
            modifier = Modifier
                .size(220.dp)
                .offset(x = 130.dp, y = 640.dp),

            )
        Image(
            painter = painterResource(id = R.drawable.img04),
            contentDescription = null,
            modifier = Modifier
                .size(60.dp)
                .offset(x = 110.dp, y = 670.dp),

            )
        OutlinedButton(
            modifier = Modifier
                .width(100.dp)
                .height(100.dp)
                .offset(x = -10.dp, y = 5.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = Color.Unspecified
            ),
            onClick = onBackClicked
        ) {
            Image(
                painter = painterResource(id = R.drawable.back_button),
                contentDescription = "戻る",
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "運んだルート",
                color = Color(0xFF55433F),
                modifier = modifier
                    .padding(top = 56.dp),
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(24.dp))

            CarryLetterDetailContent(
                uiState = uiState,
                onRetryClicked = onRetryClicked,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun CarryDetailScreenSystemUIPreview() {
    MaterialTheme {
        Scaffold { innerPadding ->
            CarryDetailScreenContent(
                uiState = CarryUiState(
                    currentUserName = "sample-user",
                    selectedLetter = CarryLetterDetailInfo(
                        letterId = "letter-123",
                        toUser = "Bob",
                        fromUser = "Alice",
                        isSurvival = true,
                        routeNodeCount = 2,
                        routeEdgeCount = 1,
                        tree = Tree()
                    )
                ),
                letterId = "letter-123",
                onBackClicked = {},
                onRetryClicked = {},
                innerPadding = innerPadding
            )
        }
    }
}

/**
 * 詳細データの読み込み状態に応じて表示内容を切り替える。
 */
@Composable
private fun CarryLetterDetailContent(
    uiState: CarryUiState,
    onRetryClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    when {
        uiState.isDetailLoading -> {
            Column(
                modifier = modifier,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
            }
        }

        uiState.errorMessage != null -> {
            Column(modifier = modifier) {
                Text(
                    text = uiState.errorMessage,
                    color = MaterialTheme.colorScheme.error
                )
                OutlinedButton(
                    modifier = Modifier.padding(top = 12.dp),
                    onClick = onRetryClicked
                ) {
                    Text("再試行")
                }
            }
        }

        uiState.selectedLetter == null -> {
            Text(
                modifier = modifier,
                text = "手紙の詳細はありません",
                color = Color(0xFF55433F),
            )
        }

        else -> {
            CarryLetterDetail(
                letter = uiState.selectedLetter,
                currentUserName = uiState.currentUserName,
                modifier = modifier
            )
        }
    }
}

/**
 * 運搬中の手紙で見せてよいメタ情報だけを表示する。
 */
@Composable
private fun CarryLetterDetail(
    letter: CarryLetterDetailInfo,
    currentUserName: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            "差出人: ${letter.fromUser}",
            color = Color(0xFF55433F),
        )
        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = "宛先: ${letter.toUser}",
            color = Color(0xFF55433F)
        )
        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = if (letter.isSurvival) "到達状態: 運搬中" else "到達状態: 到達済み",
            color = Color(0xFF55433F)
        )
        CarryMapScreen(
            tree = letter.tree,
            currentUserName = currentUserName,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 36.dp)
        )
    }
}