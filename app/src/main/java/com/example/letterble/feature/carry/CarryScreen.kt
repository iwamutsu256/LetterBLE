/**
 * CarryScreen.kt
 *
 * 役割:
 * - 運搬中の手紙一覧を表示する
 * - 手紙選択イベントを画面遷移へつなぐ
 */
package com.example.letterble.feature.carry

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.letterble.di.AppContainer

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

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "運搬中の手紙",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = if (uiState.currentUserName.isBlank()) {
                "現在のユーザー: 未登録"
            } else {
                "現在のユーザー: ${uiState.currentUserName}"
            },
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        CarryingLetterList(
            uiState = uiState,
            onRetryClicked = viewModel::loadCarryingLetters,
            onLetterClicked = onLetterClicked,
            modifier = Modifier.weight(1f)
        )

        OutlinedButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            onClick = onBackClicked
        ) {
            Text("戻る")
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
                        onClick = { onLetterClicked(letter.letterId) }
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text("宛先: ${letter.toUser}")
                            Text(
                                text = "差出人: ${letter.fromUser}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}
