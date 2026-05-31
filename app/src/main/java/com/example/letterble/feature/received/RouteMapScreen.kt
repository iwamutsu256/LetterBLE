/**
 * RouteMapScreen.kt
 *
 * 役割:
 * - treeをマップ上に描画
 */
package com.example.letterble.feature.received

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.letterble.domain.model.Tree
import com.example.letterble.ui.components.LetterTreeMapView
import com.example.letterble.ui.theme.LetterBLETheme

/**
 * 受信詳細で使う経路地図。
 *
 * 受信画面では現在ユーザーの強調は不要なので、Tree をそのまま共通 Map に渡す。
 */
@Composable
fun RouteMapScreen(
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

@Preview(showSystemUi = true)
@Composable
private fun RouteMapScreenPreview() {
    LetterBLETheme {
        RouteMapScreen(tree = Tree())
    }
}
