/**
 * Theme系ファイル
 *
 * 役割:
 * - テーマ設定
 *
 * 注意:
 * - アプリロジックを書かない
 */

// TODO: デフォルトThemeを維持する
// TODO: 色・フォント変更があればここで定義する
// TODO: ロジックは書かない



package com.example.letterble.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = LetterBLEColors.TextPrimary,
    secondary = LetterBLEColors.RegisterPrimary,
    tertiary = LetterBLEColors.Accent,
    background = LetterBLEColors.AppBackground,
    surface = LetterBLEColors.AppBackground,
    onPrimary = LetterBLEColors.AppBackground,
    onSecondary = LetterBLEColors.Accent,
    onTertiary = LetterBLEColors.RegisterPrimary,
    onBackground = LetterBLEColors.TextPrimary,
    onSurface = LetterBLEColors.TextPrimary
)
@Composable
fun LetterBLETheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
