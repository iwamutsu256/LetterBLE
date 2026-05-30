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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.letterble.domain.model.Tree
import com.example.letterble.ui.components.LetterTreeMapView

/**
 * 運搬詳細で使う経路地図。
 *
 * 現在ユーザー名と一致する node と、その node から伸びる edge を強調表示する。
 */
@Composable
fun CarryMapScreen(
    tree: Tree,
    currentUserName: String,
    modifier: Modifier = Modifier
) {
    val currentUserNodeIds = tree.nodes
        .filter { node -> node.userName == currentUserName }
        .map { node -> node.id }
        .toSet()

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
            highlightedNodeIds = currentUserNodeIds,
            highlightedEdgeFromNodeIds = currentUserNodeIds,
            modifier = Modifier.matchParentSize()
        )
    }
}

@Preview(showSystemUi = true)
@Composable
private fun CarryMapScreenPreview() {
    MaterialTheme {
        CarryMapScreen(tree = Tree(), currentUserName = "me")
    }
}
