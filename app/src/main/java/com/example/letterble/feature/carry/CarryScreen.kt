/**
 * CarryScreen.kt
 *
 * 役割:
 * - 運搬中の手紙一覧を表示する
 * - 手紙選択イベントを画面遷移へつなぐ
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

// TODO: 運搬中手紙一覧をLazyColumnなどで表示する
// TODO: 画面表示時にCarryViewModel.loadCarryingLetters()を呼ぶ
// TODO: 手紙クリックでCarryViewModelの選択イベントを呼ぶ
// TODO: 仮IDではなくViewModelの選択イベントから詳細へ遷移する
/**
 * 運搬中の手紙一覧画面の最小UIを表示する。
 *
 * @param onLetterClicked 手紙が選択されたときに詳細画面へ進むためのコールバック
 * @param onBackClicked 前の画面へ戻るためのコールバック
 * @param modifier 画面全体に適用するModifier
 */
@Composable
fun CarryScreen(
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
            text = "運搬中の手紙",
            style = MaterialTheme.typography.headlineMedium
        )
        Button(
            modifier = Modifier.padding(top = 24.dp),
            onClick = { onLetterClicked("sample-carry-letter") }
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
