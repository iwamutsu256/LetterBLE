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
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    enabled: Boolean = true
) {
    Button(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        enabled = enabled,
        colors = colors,
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}
