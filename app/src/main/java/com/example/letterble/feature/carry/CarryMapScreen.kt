/**
 * CarryMapScreen.kt
 *
 * 役割:
 * - tree表示（自ノード強調あり）
 */
package com.example.letterble.feature.carry

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.letterble.domain.model.Tree
import com.example.letterble.ui.components.LetterTreeMapView

/**
 * 運搬詳細で使う経路地図。
 *
 * #75 時点では Tree 表示だけを担当し、現在ユーザーの強調は #76 で追加する。
 */
@Composable
fun CarryMapScreen(
    tree: Tree,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp),
        contentAlignment = Alignment.Center
    ) {
        if (tree.nodes.isEmpty()) {
            Text(
                text = "地図に表示できる経路情報はありません",
                style = MaterialTheme.typography.bodyMedium
            )
            return@Box
        }

        LetterTreeMapView(
            tree = tree,
            modifier = Modifier.matchParentSize()
        )
    }
}
