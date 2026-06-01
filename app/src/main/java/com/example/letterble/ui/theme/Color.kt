/**
 * Theme系ファイル
 *
 * 役割:
 * - 色設定
 *
 * 注意:
 * - アプリロジックを書かない
 */

// TODO: 色・フォント変更があればここで定義する
// TODO: ロジックは書かない



package com.example.letterble.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * アプリ全体で使う色定義。
 *
 * 画面側では Color(0x...) を直接書かず、ここに名前を付けてから参照する。
 */
object LetterBLEColors {
    val AppBackground = Color(0xFFFFFFFA)
    val TextPrimary = Color(0xFF55433F)

    val NavigationContainer = Color(0xFF000066)
    val NavigationContent = Color.White
    val NavigationDivider = Color.White

    val RegisterBackground = Color(0xFFFF6242)
    val RegisterPrimary = Color(0xFF0F0F6D)
    val RegisterCursor = Color(0xFF04041F)

    val Accent = Color(0xFFFFF01D)

    val RouteLine = Color(0xFF4F46E5)
    val HighlightedRouteLine = Color(0xFFDC2626)

    // 配達画面用
    val CarrySurvival = Color(0xFFCBFF9B)
    val CarryDelivered = Color(0xFFEEEEF5)
    val CarryItemText = Color(0xFF2D2D2D)
}
