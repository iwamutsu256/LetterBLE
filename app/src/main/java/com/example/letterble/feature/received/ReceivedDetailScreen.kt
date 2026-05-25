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

// TODO: 手紙の詳細（本文・差出人）をViewModelから取得して表示する
// TODO: tree表示の切り替え（地図/グラフ）UIを作る
// TODO: letterIdを使ってReceivedViewModelの詳細読み込みを呼ぶ
/**
 * 受信した手紙の詳細画面の最小UIを表示する。
 *
 * @param letterId 詳細表示する手紙ID
 * @param onBackClicked 前の画面へ戻るためのコールバック
 * @param modifier 画面全体に適用するModifier
 */
@Composable
fun ReceivedDetailScreen(
    letterId: String,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    ReceivedDetailScreenContent(
        letterId = letterId,
        onBackClicked = onBackClicked,
        modifier = modifier
    )
}

@Composable
fun ReceivedDetailScreenContent(
    letterId: String,
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
            text = "受信詳細",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            modifier = Modifier.padding(top = 12.dp),
            text = "ID: $letterId"
        )
        Button(
            modifier = Modifier.padding(top = 24.dp),
            onClick = onBackClicked
        ) {
            Text("戻る")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ReceivedDetailScreenContentPreview() {
    LetterBLETheme {
        ReceivedDetailScreenContent(
            letterId = "sample-received-letter",
            onBackClicked = {}
        )
    }
}
