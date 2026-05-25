/**
 * EditLetterScreen.kt
 *
 * 役割:
 * - 手紙入力UIを表示する
 * - 手紙作成の状態を画面に反映する
 *
 * 注意:
 * - 手紙作成ロジックはViewModel / UseCaseへ委譲する
 */
package com.example.letterble.feature.edit_letter

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

// TODO: 宛先入力UIを作成する
// TODO: 本文入力UIを作成する
// TODO: 下書き保存ボタンを作る
// TODO: 投函先ポスト選択ボタンを作る
// TODO: 送信ボタンを作る
// TODO: EditLetterViewModelのstateをUIに反映する
/**
 * 手紙作成画面の最小UIを表示する。
 *
 * @param onBackClicked 前の画面へ戻るためのコールバック
 * @param modifier 画面全体に適用するModifier
 */
@Composable
fun EditLetterScreen(
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    EditLetterScreenContent(
        onBackClicked = onBackClicked,
        modifier = modifier
    )
}

@Composable
fun EditLetterScreenContent(
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
            text = "手紙作成",
            style = MaterialTheme.typography.headlineMedium
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
private fun EditLetterScreenContentPreview() {
    LetterBLETheme {
        EditLetterScreenContent(onBackClicked = {})
    }
}
