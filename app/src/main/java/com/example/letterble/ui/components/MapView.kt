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
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState

private val DefaultMapCenter = LatLng(35.681236, 139.767125)
private const val DefaultMapZoom = 12f

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
