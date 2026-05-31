/**
 * CarryScreen.kt
 *
 * 役割:
 * - 運搬中の手紙一覧を表示する
 * - 手紙選択イベントを画面遷移へつなぐ
 */
package com.example.letterble.feature.carry

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.letterble.R
import com.example.letterble.di.AppContainer
import com.example.letterble.ui.components.CommonBackButton
import com.example.letterble.ui.components.CommonBottomNavigation
import com.example.letterble.ui.theme.LetterBLETheme

/**
 * 運搬中の手紙一覧画面を表示する。
 *
 * @param appContainer Repository を ViewModel に渡すための依存関係入口
 * @param onLetterClicked 手紙が選択されたときに詳細画面へ進むためのコールバック
 * @param onBackClicked 前の画面へ戻るためのコールバック
 * @param modifier 画面全体に適用するModifier
 */
@Composable
fun CarryScreen(
    navController: NavHostController,
    appContainer: AppContainer,
    onLetterClicked: (String) -> Unit,
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

    // 画面が開かれたタイミングで、現在ユーザーが運搬中の手紙を読み込む。
    LaunchedEffect(Unit) {
        viewModel.loadCarryingLetters()
    }

    BackHandler {
        onBackClicked()
    }

    CommonBottomNavigation(navController = navController) { innerPadding ->
        CarryScreenContent(
            uiState = uiState,
            onLetterClicked = onLetterClicked,
            onBackClicked = onBackClicked,
            onRetryClicked = viewModel::loadCarryingLetters,
            innerPadding = innerPadding,
            modifier = modifier
        )
    }
}

/**
 * 表示ロジックを分離したコンテンツ部分。
 */
@Composable
private fun CarryScreenContent(
    uiState: CarryUiState,
    onLetterClicked: (String) -> Unit,
    onBackClicked: () -> Unit,
    onRetryClicked: () -> Unit,
    innerPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(all = 24.dp)
            .background(Color(0xFFFFFFFA))
    ){
        Image(
            painter = painterResource(id = R.drawable.img01),
            contentDescription = null,
            modifier = Modifier
                .size(220.dp)
                .align(Alignment.TopCenter)
                .offset(x = 150.dp , y = -100.dp)
        )
        Image(
            painter = painterResource(id = R.drawable.img02),
            contentDescription = null,
            modifier = Modifier
                .size(150.dp)
                .align(Alignment.TopCenter)
                .offset(x = 50.dp , y = -100.dp)
        )
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
        ) {
            Text(
                text = "はいたつ",
                modifier = Modifier
                    .fillMaxWidth()     // ← 横幅を広げる
                    .padding(24.dp),
                color = Color(0xFF55433F),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center   // ← 中央寄せ
            )

            Spacer(modifier = Modifier.height(16.dp))

            CarryingLetterList(
                uiState = uiState,
                onRetryClicked = onRetryClicked,
                onLetterClicked = onLetterClicked,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun CarryScreenSystemUIPreview() {
    val navController = androidx.navigation.compose.rememberNavController()
    LetterBLETheme {
        CommonBottomNavigation(navController = navController) { innerPadding ->
            CarryScreenContent(
                uiState = CarryUiState(
                    currentUserName = "sample-user",
                    carryingLetters = listOf(
                        CarryLetterListItem("1", "Alice", "Bob")
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
 * 運搬中の手紙一覧の状態ごとの表示をまとめる。
 */
@Composable
private fun CarryingLetterList(
    uiState: CarryUiState,
    onRetryClicked: () -> Unit,
    onLetterClicked: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    when {
        uiState.isLoading -> {
            Box(
                modifier = modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        uiState.errorMessage != null -> {
            Column(modifier = modifier.fillMaxWidth()) {
                Text(
                    text = uiState.errorMessage,
                    color = MaterialTheme.colorScheme.error
                )
                if (uiState.currentUserName.isNotBlank()) {
                    OutlinedButton(
                        modifier = Modifier.padding(top = 12.dp),
                        onClick = onRetryClicked
                    ) {
                        Text("再試行")
                    }
                }
            }
        }

        uiState.carryingLetters.isEmpty() -> {
            Text(
                modifier = modifier.fillMaxWidth(),
                text = "運搬中の手紙はありません"
            )
        }

        else -> {
            LazyColumn(
                modifier = modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(
                    items = uiState.carryingLetters,
                    key = { letter -> letter.letterId }
                ) { letter ->
                    // 本文は一覧にも出さず、配送に必要なメタ情報だけを見せる。
                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE8E8E8),
                            contentColor = Color(0xFF0F0F6D)
                        ),
                        onClick = { onLetterClicked(letter.letterId) }
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                "宛先: ${letter.toUser}",
                                color = Color(0xFF0F0F6D),
                            )
                            Text(
                                text = "差出人: ${letter.fromUser}",
                                color = Color(0xFF0F0F6D),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}
