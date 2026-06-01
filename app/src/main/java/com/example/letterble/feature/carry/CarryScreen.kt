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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.letterble.R
import com.example.letterble.di.AppContainer
import com.example.letterble.ui.components.CommonBackButton
import com.example.letterble.ui.components.CommonBottomNavigation
import com.example.letterble.ui.theme.LetterBLEColors
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
        modifier = Modifier
            .fillMaxSize()
            .background(LetterBLEColors.AppBackground)
    ){
        // 背景の装飾円
        Image(
            painter = painterResource(id = R.drawable.img01),
            contentDescription = null,
            modifier = Modifier
                .size(220.dp)
                .align(Alignment.TopCenter)
                .offset(x = 150.dp , y = (-100).dp)
        )
        Image(
            painter = painterResource(id = R.drawable.img02),
            contentDescription = null,
            modifier = Modifier
                .size(150.dp)
                .align(Alignment.TopCenter)
                .offset(x = 50.dp , y = (-100).dp)
        )

        CommonBackButton(
            modifier = Modifier.padding(top = innerPadding.calculateTopPadding()),
            onClick = onBackClicked
        )

        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "はいたつ",
                modifier = Modifier
                    .padding(top = 48.dp, bottom = 24.dp),
                color = LetterBLEColors.TextPrimary,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp
                ),
                textAlign = TextAlign.Center
            )

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
                        CarryLetterListItem("1", "たなかたろう", "さとうじろう", isSurvival = true),
                        CarryLetterListItem("2", "やまだはなこ", "すずきいちろう", isSurvival = true),
                        CarryLetterListItem("3", "いとうしんじ", "たかはしかずお", isSurvival = true),
                        CarryLetterListItem("4", "渡辺けん", "山本ひろし", isSurvival = false),
                        CarryLetterListItem("5", "中村みき", "小林さおり", isSurvival = false),
                        CarryLetterListItem("6", "加藤たけし", "佐々木あきら", isSurvival = true),
                        CarryLetterListItem("7", "吉田まゆ", "松本よしえ", isSurvival = false)
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
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "運搬中の手紙はありません",
                    textAlign = TextAlign.Center
                )
            }
        }

        else -> {
            LazyColumn(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(
                    items = uiState.carryingLetters,
                    key = { letter -> letter.letterId }
                ) { letter ->
                    CarryLetterItem(
                        letter = letter,
                        onClick = { onLetterClicked(letter.letterId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CarryLetterItem(
    letter: CarryLetterListItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (letter.isSurvival) LetterBLEColors.CarrySurvival else LetterBLEColors.CarryDelivered
    val contentColor = LetterBLEColors.CarryItemText

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
            .height(72.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(36.dp),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左端: アイコン (icon.png 自体に枠があるため、装飾なし)
            Image(
                painter = painterResource(id = R.drawable.icon),
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                contentScale = ContentScale.Fit
            )

            // 中央: 宛先情報
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            ) {
                Text(
                    text = "${letter.toUser}へ",
                    color = contentColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = "${letter.fromUser}より",
                    color = contentColor,
                    fontSize = 11.sp
                )
            }

            // 右端: 配達状況印
            Box(
                modifier = Modifier
                    .padding(end = 4.dp)
                    .size(36.dp)
                    .border(1.dp, Color.Black, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (!letter.isSurvival) {
                    Text(
                        text = "済",
                        color = Color.Black,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
