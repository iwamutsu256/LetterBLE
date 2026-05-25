/**
 * ReceivedScreen.kt
 *
 * 役割:
 * - 受信した手紙の一覧を表示する
 * - 手紙選択イベントを画面遷移へつなぐ
 */
package com.example.letterble.feature.received

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.letterble.ui.theme.LetterBLETheme

// TODO: 受信手紙一覧をLazyColumnなどで表示する
// TODO: 画面表示時にReceivedViewModel.loadReceivedLetters()を呼ぶ
// TODO: アイテムクリックでReceivedViewModel.onLetterClicked()を呼ぶ
// TODO: 仮IDではなくViewModelの選択イベントから詳細へ遷移する
/**
 * 受信した手紙一覧画面の最小UIを表示する。
 *
 * @param onLetterClicked 手紙が選択されたときに詳細画面へ進むためのコールバック
 * @param onBackClicked 前の画面へ戻るためのコールバック
 * @param modifier 画面全体に適用するModifier
 */
@Composable
fun ReceivedScreen(
    onLetterClicked: (String) -> Unit,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    ReceivedScreenContent(
        onLetterClicked = onLetterClicked,
        onBackClicked = onBackClicked,
        modifier = modifier
    )
}

@Composable
fun ReceivedScreenContent(
    onLetterClicked: (String) -> Unit,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "受信した手紙",
            style = MaterialTheme.typography.headlineMedium
        )
        Button(
            modifier = Modifier.padding(top = 24.dp),
            onClick = { onLetterClicked("sample-received-letter") }
        ) {
            Text("詳細を開く")
        }
        Button(
            modifier = Modifier.padding(top = 8.dp),
            onClick = onBackClicked
        ) {
            Text("戻る")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ReceivedScreenContentPreview() {
    LetterBLETheme {
        ReceivedScreenContent(
            onLetterClicked = {},
            onBackClicked = {}
        )
    }
}
