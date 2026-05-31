package com.example.letterble.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.letterble.R

/**
 * 戻る共通ボタン
 *
 * @param onClick ボタン押下時に実行する処理
 * @param modifier ボタンに適用するModifier
 * @param enabled ボタンを押せる状態にするかどうか
 */
@Composable
fun CommonBackButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    IconButton(
        modifier = Modifier
            .width(48.dp)
            .height(48.dp)
            .offset(24.dp,48.dp),
        onClick = onClick,
        enabled = enabled,
    ) {
        Icon(
            painter = painterResource(id=R.drawable.back_button),
            tint = Color.Unspecified,
            contentDescription = "戻る",
            modifier = Modifier
        )
    }
}

