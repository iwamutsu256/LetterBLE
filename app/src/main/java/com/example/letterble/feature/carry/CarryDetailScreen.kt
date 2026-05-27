/**
 * CarryDetailScreen.kt
 *
 * 役割:
 * - 運搬中の手紙の詳細を表示する
 * - 経路ツリー表示へつなぐ
 */
package com.example.letterble.feature.carry

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "運搬詳細",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            modifier = Modifier.padding(top = 12.dp),
            text = "ID: $letterId"
        )

        Spacer(modifier = Modifier.height(24.dp))

        CarryLetterDetailContent(
            uiState = uiState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = onBackClicked
        ) {
            Text("戻る")
        }
    }
}

/**
 * 詳細データの読み込み状態に応じて表示内容を切り替える。
 */
@Composable
private fun CarryLetterDetailContent(
    uiState: CarryUiState,
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
            Text(
                modifier = modifier,
                text = uiState.errorMessage,
                color = MaterialTheme.colorScheme.error
            )
        }

        uiState.selectedLetter == null -> {
            Text(
                modifier = modifier,
                text = "手紙の詳細はありません"
            )
        }

        else -> {
            CarryLetterDetail(
                letter = uiState.selectedLetter,
                currentUserName = uiState.currentUserName
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
    Column(modifier = modifier.fillMaxWidth()) {
        Text("差出人: ${letter.fromUser}")
        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = "宛先: ${letter.toUser}"
        )
        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = if (letter.isSurvival) "到達状態: 運搬中" else "到達状態: 到達済み"
        )
        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = "経路概要: ${letter.routeNodeCount}地点 / ${letter.routeEdgeCount}区間"
        )
        CarryMapScreen(
            tree = letter.tree,
            currentUserName = currentUserName,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}
