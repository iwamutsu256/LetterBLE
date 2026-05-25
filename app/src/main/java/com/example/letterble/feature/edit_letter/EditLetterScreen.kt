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
import androidx.compose.ui.unit.dp

// TODO: 宛先入力UIを作成する
// TODO: 本文入力UIを作成する
// TODO: 下書き保存ボタンを作る
// TODO: 投函先ポスト選択ボタンを作る
// TODO: 送信ボタンを作る
// TODO: EditLetterViewModelのstateをUIに反映する
@Composable
fun EditLetterScreen(
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
