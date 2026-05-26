/**
 * ReceivedDetailScreen.kt
 *
 * 役割:
 * - 受信した手紙の詳細を表示する
 * - 経路ツリー表示へつなぐ
 */
package com.example.letterble.feature.received

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.letterble.di.AppContainer
import com.example.letterble.ui.components.CommonButton

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
    modifier: Modifier = Modifier
) {
    /**
     * 詳細画面でもRepositoryは直接触らず、FactoryからViewModelへ渡す。
     *
     * ReceivedScreenと同じFactoryを使うことで、
     * 一覧と詳細で「受信手紙に関する状態はReceivedViewModelが持つ」という形を揃えている。
     */
    val viewModel: ReceivedViewModel = viewModel(
        factory = ReceivedViewModelFactory(
            userRepository = appContainer.userRepository,
            letterRepository = appContainer.letterRepository,
            locationRepository = appContainer.locationRepository
        )
    )

    val uiState by viewModel.uiState.collectAsState()
    val detailState = uiState.detailState

    /**
     * letterIdが変わったタイミングで詳細を読み込む。
     *
     * Composableの本文で直接loadLetterDetail()を呼ぶと、再描画のたびに読み込みが走る。
     * LaunchedEffect(letterId)にすることで、同じletterIdでは一度だけ実行される。
     */
    LaunchedEffect(letterId) {
        viewModel.loadLetterDetail(letterId)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "受信詳細",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        when {
            detailState.isLoading -> {
                ReceivedDetailLoadingContent()
            }

            detailState.errorMessage != null -> {
                ReceivedDetailErrorContent(
                    errorMessage = detailState.errorMessage,
                    onRetryClicked = { viewModel.loadLetterDetail(letterId) }
                )
            }

            detailState.detail != null -> {
                ReceivedDetailContent(detail = detailState.detail)
            }

            else -> {
                Text(
                    text = "手紙の詳細を読み込んでいます",
                    style = MaterialTheme.typography.bodyMedium
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
 * 詳細読み込み中の表示。
 */
@Composable
private fun ReceivedDetailLoadingContent() {
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
 * 詳細読み込み失敗時の表示。
 */
@Composable
private fun ReceivedDetailErrorContent(
    errorMessage: String,
    onRetryClicked: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
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
 * 読み込み済みの手紙詳細。
 *
 * 本文は受信者には読める情報なので、この画面では全文を表示する。
 * 一方、地図そのものは#60で扱うため、ここでは経路概要だけに留めている。
 */
@Composable
private fun ReceivedDetailContent(
    detail: ReceivedLetterDetail
) {
    val letter = detail.letter

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        DetailCard(title = "差出人", value = letter.fromUser.ifBlank { "不明" })
        DetailCard(title = "宛先", value = letter.toUser.ifBlank { "不明" })
        DetailCard(title = "本文", value = letter.sentence.ifBlank { "本文なし" })
        DetailCard(title = "経路概要", value = detail.routeSummary)
    }
}

/**
 * 詳細画面の項目表示。
 *
 * Textを縦に並べるだけでも表示できるが、項目ごとにCardへ分けると、
 * 本文・差出人・経路概要のまとまりが読み取りやすい。
 */
@Composable
private fun DetailCard(
    title: String,
    value: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                modifier = Modifier.padding(top = 8.dp),
                text = value,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
