/**
 * Theme系ファイル
 *
 * 役割:
 * - フォント設定
 *
 * 注意:
 * - アプリロジックを書かない
 */

package com.example.letterble.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.letterble.R

object LetterBLEFontFamilies {
    val PostNoBillsColombo = FontFamily(
        Font(resId = R.font.post_no_bills_colombo_extrabold, weight = FontWeight.ExtraBold)
    )

    val NotoSansJp = FontFamily(
        Font(resId = R.font.noto_sans_jp_thin, weight = FontWeight.Thin),
        Font(resId = R.font.noto_sans_jp_extra_light, weight = FontWeight.ExtraLight),
        Font(resId = R.font.noto_sans_jp_light, weight = FontWeight.Light),
        Font(resId = R.font.noto_sans_jp_regular, weight = FontWeight.Normal),
        Font(resId = R.font.noto_sans_jp_medium, weight = FontWeight.Medium),
        Font(resId = R.font.noto_sans_jp_semi_bold, weight = FontWeight.SemiBold),
        Font(resId = R.font.noto_sans_jp_bold, weight = FontWeight.Bold),
        Font(resId = R.font.noto_sans_jp_extra_bold, weight = FontWeight.ExtraBold),
        Font(resId = R.font.noto_sans_jp_black, weight = FontWeight.Black),
    )
}

val LetterBleFontFamily = LetterBLEFontFamilies.PostNoBillsColombo
val NotoSansJpFontFamily = LetterBLEFontFamilies.NotoSansJp

object LetterBLEFontSize {
    val Display = 50.sp
    val Title = 40.sp
    val Headline = 28.sp
    val SectionTitle = 30.sp
    val Button = 20.sp
    val Body = 16.sp
    val Label = 12.sp
}

object LetterBLETextStyles {
    val EnglishDisplay = TextStyle(
        fontFamily = LetterBLEFontFamilies.PostNoBillsColombo,
        fontWeight = FontWeight.ExtraBold,
        fontSize = LetterBLEFontSize.Display,
        lineHeight = 60.sp,
        letterSpacing = 0.sp
    )

    val EnglishTitle = TextStyle(
        fontFamily = LetterBLEFontFamilies.PostNoBillsColombo,
        fontWeight = FontWeight.ExtraBold,
        fontSize = LetterBLEFontSize.Title,
        lineHeight = 48.sp,
        letterSpacing = 0.sp
    )

    val EnglishButton = TextStyle(
        fontFamily = LetterBLEFontFamilies.PostNoBillsColombo,
        fontWeight = FontWeight.ExtraBold,
        fontSize = LetterBLEFontSize.Button,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    )
}

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = NotoSansJpFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = LetterBLEFontSize.Display,
        lineHeight = 60.sp,
        letterSpacing = 0.sp
    ),
    displayMedium = TextStyle(
        fontFamily = NotoSansJpFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = LetterBLEFontSize.Display,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = NotoSansJpFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = LetterBLEFontSize.Title,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = NotoSansJpFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = LetterBLEFontSize.Title,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = NotoSansJpFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = LetterBLEFontSize.Headline,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = NotoSansJpFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = LetterBLEFontSize.SectionTitle,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = NotoSansJpFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = LetterBLEFontSize.SectionTitle,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = NotoSansJpFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = LetterBLEFontSize.SectionTitle,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    titleSmall = TextStyle(
        fontFamily = NotoSansJpFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = LetterBLEFontSize.Body,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = NotoSansJpFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = LetterBLEFontSize.Body,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = NotoSansJpFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = LetterBLEFontSize.Body,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),
    bodySmall = TextStyle(
        fontFamily = NotoSansJpFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = LetterBLEFontSize.Label,
        lineHeight = 16.sp,
        letterSpacing = 0.sp
    ),
    labelLarge = TextStyle(
        fontFamily = NotoSansJpFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = LetterBLEFontSize.Button,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = NotoSansJpFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = LetterBLEFontSize.Label,
        lineHeight = 16.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = NotoSansJpFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)
