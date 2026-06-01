package com.example.letterble.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * 手紙風の背景を表示するコンポーネント。
 */
@Composable
fun LetterPaper(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        color = Color(0xFFF9F6EF),
        shadowElevation = 2.dp,
        shape = RoundedCornerShape(4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            // 背景の点線枠。
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .padding(12.dp)
                    .drawBehind {
                        val stroke = Stroke(
                            width = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )
                        drawRect(
                            color = Color.Gray.copy(alpha = 0.3f),
                            style = stroke
                        )
                    }
            )

            // 実際のコンテンツ。
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .padding(16.dp)
            ) {
                content()
            }
        }
    }
}
