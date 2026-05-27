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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
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
    var hasLocationPermission by remember {
        mutableStateOf(context.hasLocationPermission())
    }
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions.values.any { it }
        if (!hasLocationPermission) {
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

    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            viewModel.loadNearbyPosts()
        }
    }

    if (uiState.showConfirmDialog) {
        val selectedPost = uiState.selectedPost
        AlertDialog(
            onDismissRequest = viewModel::onConfirmDialogDismissed,
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "ポスト選択",
            style = MaterialTheme.typography.headlineMedium
        )

        when {
            uiState.isLoading -> {
                CircularProgressIndicator(modifier = Modifier.padding(top = 24.dp))
            }

            !hasLocationPermission -> {
                Text(
                    text = "近くのポストを探すには位置情報の許可が必要です",
                    modifier = Modifier.padding(top = 24.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            uiState.posts.isNotEmpty() -> {
                PostSelectMap(
                    posts = uiState.posts,
                    currentPosition = uiState.currentLatLng(),
                    selectedPost = uiState.selectedPost,
                    hasLocationPermission = hasLocationPermission,
                    onPostClicked = viewModel::onPostSelected,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(top = 24.dp)
                )
            }
        }

        uiState.message?.let { message ->
            Text(
                text = message,
                modifier = Modifier.padding(top = 16.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        CommonButton(
            text = if (hasLocationPermission) "再取得" else "位置情報を許可して検索",
            modifier = Modifier.padding(top = 16.dp),
            enabled = !uiState.isLoading,
            onClick = {
                if (hasLocationPermission) {
                    viewModel.loadNearbyPosts()
                } else {
                    locationPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            }
        )
        CommonButton(
            text = "戻る",
            modifier = Modifier.padding(top = 8.dp),
            onClick = onBackClicked
        )
    }
}

/**
 * 画面表示時点で位置情報権限があるかを確認する。
 */
private fun Context.hasLocationPermission(): Boolean {
    val fineLocationGranted = ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    val coarseLocationGranted = ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    return fineLocationGranted || coarseLocationGranted
}

@Composable
private fun PostSelectMap(
    posts: List<Post>,
    currentPosition: LatLng?,
    selectedPost: Post?,
    hasLocationPermission: Boolean,
    onPostClicked: (Post) -> Unit,
    modifier: Modifier = Modifier
) {
    val initialCenter = currentPosition ?: posts.firstOrNull()?.toLatLng() ?: DefaultPostMapCenter
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialCenter, 15f)
    }

    LaunchedEffect(posts, currentPosition) {
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
            properties = MapProperties(isMyLocationEnabled = hasLocationPermission)
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
