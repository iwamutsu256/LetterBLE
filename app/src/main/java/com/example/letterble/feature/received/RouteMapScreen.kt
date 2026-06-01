/**
 * RouteMapScreen.kt
 *
 * 役割:
 * - treeをマップ上に描画
 */
package com.example.letterble.feature.received

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.letterble.domain.model.Tree
import com.example.letterble.ui.components.LetterTreeMapView
import com.example.letterble.ui.theme.LetterBLETheme
import com.google.maps.android.compose.MapUiSettings

/**
 * 受信詳細で使う経路地図。
 *
 * 表示専用（操作不能）とし、エリア全体をクリック可能にして詳細画面へ遷移させる。
 */
@Composable
fun RouteMapScreen(
    tree: Tree,
    onMapClicked: () -> Unit,
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

        // 地図本体（ジェスチャーをすべて無効化）
        LetterTreeMapView(
            tree = tree,
            modifier = Modifier.matchParentSize(),
            uiSettings = remember {
                MapUiSettings(
                    scrollGesturesEnabled = false,
                    zoomGesturesEnabled = false,
                    tiltGesturesEnabled = false,
                    rotationGesturesEnabled = false,
                    compassEnabled = false,
                    mapToolbarEnabled = false,
                    myLocationButtonEnabled = false,
                    zoomControlsEnabled = false
                )
            }
        )

        // 地図の上に透明なレイヤーを重ねて、クリックイベントを横取りする。
        // これにより、ピンのタップ判定（フォーカスアニメーション）を無効化し、
        // かつ地図エリア全体のクリックで確実に画面遷移を行えるようにする。
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null // 地図上での波紋エフェクトが不要な場合は null、必要な場合はデフォルト（無指定）にする
                ) {
                    onMapClicked()
                }
        )
    }
}

@Preview(showSystemUi = true)
@Composable
private fun RouteMapScreenPreview() {
    LetterBLETheme {
        RouteMapScreen(tree = Tree(), onMapClicked = {})
    }
}
