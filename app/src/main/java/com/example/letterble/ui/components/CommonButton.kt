/**
 * CommonButton.kt
 *
 * 役割:
 * - アプリ内で再利用するボタンUIを定義する
 * - 画面ごとのボタン表現を揃える
 *
 * 注意:
 * - ViewModelに依存しない
 */
package com.example.letterble.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// TODO: 必要になったら共通Cardなどの再利用UIを追加する
// TODO: MapViewなどの画面横断コンポーネントはViewModel非依存で定義する
/**
 * 横幅いっぱいに表示する共通ボタン。
 *
 * @param text ボタンに表示する文言
 * @param onClick ボタン押下時に実行する処理
 * @param modifier ボタンに適用するModifier
 * @param enabled ボタンを押せる状態にするかどうか
 */
@Composable
fun CommonButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        enabled = enabled,
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Text(text = text)
    }
}
