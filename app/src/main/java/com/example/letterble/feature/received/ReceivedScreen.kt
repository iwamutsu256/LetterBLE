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
import androidx.compose.ui.unit.dp

// TODO: 受信手紙一覧をLazyColumnなどで表示する
// TODO: 画面表示時にReceivedViewModel.loadReceivedLetters()を呼ぶ
// TODO: アイテムクリックでReceivedViewModel.onLetterClicked()を呼ぶ
// TODO: 仮IDではなくViewModelの選択イベントから詳細へ遷移する
@Composable
fun ReceivedScreen(
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
