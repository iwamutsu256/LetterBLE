/**
 * CarryDetailScreen.kt
 *
 * 役割:
 * - 運搬中の手紙の詳細を表示する
 * - 経路ツリー表示へつなぐ
 */
package com.example.letterble.feature.carry

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
import androidx.compose.ui.unit.dp

// TODO: 手紙の到達状態をViewModelから取得して表示する
// TODO: tree表示UIを作る
// TODO: letterIdを使ってCarryViewModelの詳細読み込みを呼ぶ
/**
 * 運搬中の手紙の詳細画面の最小UIを表示する。
 *
 * @param letterId 詳細表示する手紙ID
 * @param onBackClicked 前の画面へ戻るためのコールバック
 * @param modifier 画面全体に適用するModifier
 */
@Composable
fun CarryDetailScreen(
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
            text = "運搬詳細",
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
