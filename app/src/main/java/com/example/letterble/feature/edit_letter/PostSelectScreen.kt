/**
 * PostSelectScreen.kt
 *
 * 役割:
 * - ポスト選択UI
 * - 現在地周辺のポスト一覧表示
 */
package com.example.letterble.feature.edit_letter

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.letterble.R
import com.example.letterble.di.AppContainer
import com.example.letterble.domain.model.Post
import com.example.letterble.ui.components.CommonButton
import com.example.letterble.ui.components.LetterMapView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

/**
 * 現在地周辺のポスト候補を表示する最小UI。
 */
@Composable
fun PostSelectScreen(
    appContainer: AppContainer,
    onBackClicked: () -> Unit,
    onSubmitted: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PostSelectViewModel = viewModel(
        factory = appContainer.postSelectViewModelFactory()
    )
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var hasFineLocationPermission by remember {
        mutableStateOf(context.hasFineLocationPermission())
    }
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasFineLocationPermission =
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true || context.hasFineLocationPermission()
        if (!hasFineLocationPermission) {
            viewModel.onLocationPermissionDenied()
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                PostSelectEvent.NavigateHome -> onSubmitted()
            }
        }
    }

    LaunchedEffect(hasFineLocationPermission) {
        if (hasFineLocationPermission) {
            viewModel.loadNearbyPosts()
        }
    }

    BackHandler(enabled = uiState.isSubmitting) {
        // 投函中は coroutine の成功処理で下書き削除と画面遷移を完了させるため、戻る操作を無視する。
    }

    if (uiState.showConfirmDialog) {
        val selectedPost = uiState.selectedPost
        AlertDialog(
            onDismissRequest = {
                if (!uiState.isSubmitting) {
                    viewModel.onConfirmDialogDismissed()
                }
            },
            title = { Text("投函先の確認") },
            text = {
                Text(
                    text = if (selectedPost == null) {
                        "投函先を選択してください"
                    } else {
                        "${selectedPost.name}\n${selectedPost.latitude}, ${selectedPost.longitude}"
                    }
                )
            },
            confirmButton = {
                TextButton(
                    enabled = !uiState.isSubmitting && selectedPost != null,
                    onClick = viewModel::onSubmitConfirmed
                ) {
                    Text(if (uiState.isSubmitting) "投函中" else "投函")
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !uiState.isSubmitting,
                    onClick = viewModel::onConfirmDialogDismissed
                ) {
                    Text("戻る")
                }
            }
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center).padding(24.dp))
                }

                !hasFineLocationPermission -> {
                    Text(
                        text = "1km以内のポスト検索には正確な位置情報の許可が必要です",
                        modifier = Modifier
                            .align(Alignment.Center).padding(24.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                uiState.posts.isNotEmpty() -> {
                    PostSelectMap(
                        posts = uiState.posts,
                        currentPosition = uiState.currentLatLng(),
                        selectedPost = uiState.selectedPost,
                        hasFineLocationPermission = hasFineLocationPermission,
                        onPostClicked = viewModel::onPostSelected,
                        modifier = Modifier
                            .fillMaxSize()
                    )
                }
            }
        }
        IconButton(
            modifier = Modifier
                .width(48.dp)
                .height(48.dp)
                .offset(24.dp,48.dp),
            onClick = onBackClicked,
            enabled = !uiState.isSubmitting,
        ) {
            Icon(
                painter = painterResource(id=R.drawable.back_button),
                tint = null,
                contentDescription = "戻る",
                modifier = Modifier
            )
        }
        Image(
            painter = painterResource(id = R.drawable.img07),
            contentDescription = null,
            modifier = Modifier
                .size(400.dp)
                .offset(120.dp,(-144).dp)
        )
        Text(
            text = "とうかんする場所\nを選ぶ",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset((-24).dp,40.dp)
        )
        uiState.message?.let { message ->
            Text(
                text = message,
                modifier = Modifier.align(Alignment.Center).padding(16.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }


        // 投函ボタン (下部中央)
        if (uiState.selectedPost != null) {
            CommonButton(
                text = "ここに投函する",
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
                    .fillMaxWidth(0.8f),
                enabled = !uiState.isSubmitting,
                onClick = viewModel::onPostSubmitClicked
            )
        }
    }
}

/**
 * 1km検索に必要な正確な位置情報権限があるかを確認する。
 */
private fun Context.hasFineLocationPermission(): Boolean {
    return ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
}

@Composable
private fun PostSelectMap(
    posts: List<Post>,
    currentPosition: LatLng?,
    selectedPost: Post?,
    hasFineLocationPermission: Boolean,
    onPostClicked: (Post) -> Unit,
    modifier: Modifier = Modifier
) {
    val initialCenter = currentPosition ?: posts.firstOrNull()?.toLatLng() ?: DefaultPostMapCenter
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialCenter, 15f)
    }
    var isMapLoaded by remember { mutableStateOf(false) }

    LaunchedEffect(isMapLoaded, posts, currentPosition) {
        if (!isMapLoaded) {
            return@LaunchedEffect
        }

        val positions = buildList {
            currentPosition?.let(::add)
            posts.forEach { post -> add(post.toLatLng()) }
        }
        if (positions.isEmpty()) {
            return@LaunchedEffect
        }

        // 現在地と候補ピンを初期表示範囲に収め、地図上で選びやすくする。
        val cameraUpdate = if (positions.size == 1 || positions.allSamePosition()) {
            CameraUpdateFactory.newLatLngZoom(positions.first(), 16f)
        } else {
            CameraUpdateFactory.newLatLngBounds(positions.toBounds(), PostMapBoundsPadding)
        }
        cameraPositionState.move(cameraUpdate)
    }

    Box(
        modifier = modifier.height(420.dp),
        contentAlignment = Alignment.Center
    ) {
        LetterMapView(
            modifier = Modifier.matchParentSize(),
            initialCenter = initialCenter,
            initialZoom = 15f,
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = hasFineLocationPermission),
            onMapLoaded = { isMapLoaded = true }
        ) {
            posts.forEach { post ->
                val isSelected = post.id == selectedPost?.id
                Marker(
                    state = MarkerState(position = post.toLatLng()),
                    title = post.name,
                    snippet = "${post.latitude}, ${post.longitude}",
                    icon = BitmapDescriptorFactory.defaultMarker(
                        if (isSelected) BitmapDescriptorFactory.HUE_AZURE else BitmapDescriptorFactory.HUE_RED
                    ),
                    onClick = {
                        onPostClicked(post)
                        true
                    }
                )
            }
        }

        if (posts.isEmpty()) {
            Text(
                text = "地図に表示できるポストはありません",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

private val DefaultPostMapCenter = LatLng(35.681236, 139.767125)
private const val PostMapBoundsPadding = 96

private fun PostSelectUiState.currentLatLng(): LatLng? {
    val latitude = currentLatitude ?: return null
    val longitude = currentLongitude ?: return null
    return LatLng(latitude, longitude)
}

private fun Post.toLatLng(): LatLng {
    return LatLng(latitude, longitude)
}

private fun List<LatLng>.toBounds(): LatLngBounds {
    val builder = LatLngBounds.Builder()
    forEach { position -> builder.include(position) }
    return builder.build()
}

private fun List<LatLng>.allSamePosition(): Boolean {
    val first = first()
    return all { position ->
        position.latitude == first.latitude && position.longitude == first.longitude
    }
}

@Preview(showBackground = true)
@Composable
private fun PostSelectScreenContentPreview() {
    // UIのレイアウト確認用の簡易プレビュー
    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Box(
                // 地図の代わり
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFFFFFCA))
            )
            IconButton(
                modifier = Modifier
                    .width(48.dp)
                    .height(48.dp)
                    .offset(x = 24.dp, y = 24.dp),
                onClick = {}
            ) {
                Icon(
                    painter = painterResource(id=R.drawable.back_button),
                    tint = null,
                    contentDescription = "戻る",
                    modifier = Modifier
                )
            }
            Image(
                painter = painterResource(id = R.drawable.img07),
                contentDescription = null,
                modifier = Modifier
                    .size(400.dp)
                    .offset(120.dp, (-192).dp)
            )
            Text(
                text = "投函する場所\nを選ぶ",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset((-24).dp, 16.dp)
            )



            // 投函ボタン (ピン選択時を想定)
            CommonButton(
                text = "ここに投函する",
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
                    .fillMaxWidth(0.8f),
                onClick = {}
            )
        }
    }
}
