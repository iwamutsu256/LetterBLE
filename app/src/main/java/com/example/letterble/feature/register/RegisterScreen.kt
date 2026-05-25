/**
 * RegisterScreen.kt
 *
 * 役割:
 * - ユーザー登録UIを表示する
 * - ユーザー入力を受け付ける
 *
 * 注意:
 * - 状態はViewModelから取得する
 * - Repositoryへ直接アクセスしない
 */
package com.example.letterble.feature.register

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

// TODO: TextFieldでユーザー名入力を受け付ける
// TODO: RegisterViewModelのstateをcollectしてUIに反映する
// TODO: ボタンでRegisterViewModelの登録イベントを呼び出す
// TODO: 位置情報・Bluetoothの権限要求UIを表示する
/**
 * ユーザー登録画面の最小UIを表示する。
 *
 * @param onRegistered 登録完了後にホーム画面へ進むためのコールバック
 * @param modifier 画面全体に適用するModifier
 */
@Composable
fun RegisterScreen(
    onRegistered: () -> Unit,
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
            text = "ユーザー登録",
            style = MaterialTheme.typography.headlineMedium
        )
        Button(
            modifier = Modifier.padding(top = 24.dp),
            onClick = onRegistered
        ) {
            Text("登録して開始")
        }
    }
}
