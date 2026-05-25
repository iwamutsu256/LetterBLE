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
