/**
 * 共通UIコンポーネント
 *
 * 役割:
 * - 再利用UI
 * - Map
 */
package com.example.letterble.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.letterble.domain.model.Tree
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState

private val DefaultMapCenter = LatLng(35.681236, 139.767125)
private const val DefaultMapZoom = 12f
private const val DefaultRouteLineWidth = 8f

/**
 * Google Maps SDK を Compose から扱うための共通 Map コンポーネント。
 *
 * 画面側はこの Composable だけを使い、GoogleMap の初期設定を各画面へ散らさない。
 */
@Composable
fun LetterMapView(
    modifier: Modifier = Modifier,
    initialCenter: LatLng = DefaultMapCenter,
    initialZoom: Float = DefaultMapZoom,
    properties: MapProperties = MapProperties(),
    uiSettings: MapUiSettings = remember {
        MapUiSettings(
            compassEnabled = true,
            mapToolbarEnabled = false,
            myLocationButtonEnabled = false,
            zoomControlsEnabled = false
        )
    },
    content: @Composable () -> Unit = {}
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialCenter, initialZoom)
    }

    GoogleMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = properties,
        uiSettings = uiSettings,
        content = content
    )
}

/**
 * Tree の node を marker、edge を line として表示する共通 Map API。
 *
 * Tree の構造をそのまま受け取ることで、受信経路画面と運搬経路画面で同じ描画処理を使い回す。
 */
@Composable
fun LetterTreeMapView(
    tree: Tree,
    modifier: Modifier = Modifier,
    highlightedNodeIds: Set<String> = emptySet(),
    highlightedEdgeFromNodeIds: Set<String> = emptySet(),
    routeLineColor: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color(0xFF4F46E5),
    highlightedRouteLineColor: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color(0xFFDC2626),
    markerHue: Float = BitmapDescriptorFactory.HUE_AZURE,
    highlightedMarkerHue: Float = BitmapDescriptorFactory.HUE_RED
) {
    val firstNodePosition = tree.nodes.firstOrNull()?.toLatLng()

    LetterMapView(
        modifier = modifier,
        initialCenter = firstNodePosition ?: DefaultMapCenter,
        initialZoom = if (firstNodePosition == null) DefaultMapZoom else 14f
    ) {
        TreeEdges(
            tree = tree,
            highlightedEdgeFromNodeIds = highlightedEdgeFromNodeIds,
            routeLineColor = routeLineColor,
            highlightedRouteLineColor = highlightedRouteLineColor
        )
        TreeMarkers(
            tree = tree,
            highlightedNodeIds = highlightedNodeIds,
            markerHue = markerHue,
            highlightedMarkerHue = highlightedMarkerHue
        )
    }
}

/**
 * Tree.nodes を地図上の marker として描画する。
 */
@Composable
private fun TreeMarkers(
    tree: Tree,
    highlightedNodeIds: Set<String>,
    markerHue: Float,
    highlightedMarkerHue: Float
) {
    tree.nodes.forEach { node ->
        val isHighlighted = node.id in highlightedNodeIds
        Marker(
            state = MarkerState(position = node.toLatLng()),
            title = node.userName.ifBlank { node.id },
            snippet = "${node.latitude}, ${node.longitude}",
            icon = BitmapDescriptorFactory.defaultMarker(
                if (isHighlighted) highlightedMarkerHue else markerHue
            )
        )
    }
}

/**
 * Tree.edges を node 同士を結ぶ line として描画する。
 *
 * 壊れた edge が混ざっていても、対応する node が見つからない edge は描画せずに無視する。
 */
@Composable
private fun TreeEdges(
    tree: Tree,
    highlightedEdgeFromNodeIds: Set<String>,
    routeLineColor: androidx.compose.ui.graphics.Color,
    highlightedRouteLineColor: androidx.compose.ui.graphics.Color
) {
    val nodesById = tree.nodes.associateBy { node -> node.id }

    tree.edges.forEach { edge ->
        val fromNode = nodesById[edge.fromNodeId] ?: return@forEach
        val toNode = nodesById[edge.toNodeId] ?: return@forEach
        // 運搬画面の仕様では「自分から伸びる edge」だけを強調する。
        val isHighlighted = edge.fromNodeId in highlightedEdgeFromNodeIds

        Polyline(
            points = listOf(fromNode.toLatLng(), toNode.toLatLng()),
            color = if (isHighlighted) highlightedRouteLineColor else routeLineColor,
            width = if (isHighlighted) DefaultRouteLineWidth * 1.5f else DefaultRouteLineWidth
        )
    }
}

private fun com.example.letterble.domain.model.Node.toLatLng(): LatLng {
    return LatLng(latitude, longitude)
}
