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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.letterble.R
import com.example.letterble.di.AppContainer
import com.example.letterble.domain.model.Post
import com.example.letterble.ui.components.CommonBackButton
import com.example.letterble.ui.components.CommonButton
import com.example.letterble.ui.components.LetterMapView
import com.example.letterble.ui.theme.LetterBLEFontFamilies
import com.example.letterble.ui.theme.LetterBLEFontSize
import com.example.letterble.ui.theme.LetterBLETheme
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

    Scaffold { innerPadding ->
        PostSelectScreenContent(
            uiState = uiState,
            hasFineLocationPermission = hasFineLocationPermission,
            onLocationPermissionRequest = {
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            },
            onPostClicked = viewModel::onPostSelected,
            onBackClicked = onBackClicked,
            onRetryPostsClicked = viewModel::loadNearbyPosts,
            onSubmitClicked = viewModel::onPostSubmitClicked,
            innerPadding = innerPadding,
            modifier = modifier
        )
    }
}

/**
 * 表示ロジックを分離したコンテンツ部分。
 */
@Composable
private fun PostSelectScreenContent(
    uiState: PostSelectUiState,
    hasFineLocationPermission: Boolean,
    onLocationPermissionRequest: () -> Unit,
    onPostClicked: (Post) -> Unit,
    onBackClicked: () -> Unit,
    onRetryPostsClicked: () -> Unit,
    onSubmitClicked: () -> Unit,
    innerPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        val errorMessage = uiState.errorMessage
        val message = uiState.message

        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp)
                    )
                }

                !hasFineLocationPermission -> {
                    PostSelectStatusContent(
                        message = "1km以内のポスト検索には正確な位置情報の許可が必要です",
                        isError = true,
                        buttonText = "正確な位置情報を許可して検索",
                        modifier = Modifier.align(Alignment.Center),
                        onButtonClick = onLocationPermissionRequest
                    )
                }

                errorMessage != null && uiState.canRetryPostSearch -> {
                    PostSelectStatusContent(
                        message = errorMessage,
                        isError = true,
                        buttonText = "再試行",
                        modifier = Modifier.align(Alignment.Center),
                        onButtonClick = onRetryPostsClicked
                    )
                }

                uiState.posts.isNotEmpty() -> {
                    PostSelectMap(
                        posts = uiState.posts,
                        currentPosition = uiState.currentLatLng(),
                        selectedPost = uiState.selectedPost,
                        hasFineLocationPermission = hasFineLocationPermission,
                        onPostClicked = onPostClicked,
                        modifier = Modifier
                            .fillMaxSize()
                    )
                }

                message != null -> {
                    PostSelectStatusContent(
                        message = message,
                        isError = false,
                        buttonText = "再検索",
                        modifier = Modifier.align(Alignment.Center),
                        onButtonClick = onRetryPostsClicked
                    )
                }
            }
        }
        CommonBackButton(
            modifier = Modifier.padding(top = innerPadding.calculateTopPadding()),
            onClick = onBackClicked,
            enabled = !uiState.isSubmitting
        )
        Image(
            painter = painterResource(id = R.drawable.img07),
            contentDescription = null,
            modifier = Modifier
                .size(400.dp)
                .offset(120.dp, (-144).dp)
        )
        Text(
            text = "とうかんする場所\nを選ぶ",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontFamily = LetterBLEFontFamilies.NotoSansJp,
                fontWeight = FontWeight.Black,
                fontSize = LetterBLEFontSize.Headline,
                lineHeight = LetterBLEFontSize.SectionTitle
            ),
            textAlign = TextAlign.End,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = innerPadding.calculateTopPadding())
                .offset((-24).dp, 40.dp)
        )
        if (errorMessage != null && !uiState.canRetryPostSearch && hasFineLocationPermission) {
            PostSelectStatusContent(
                message = errorMessage,
                isError = true,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        // 投函ボタン (下部中央)
        if (uiState.selectedPost != null) {
            CommonButton(
                text = "ここに投函する",
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = innerPadding.calculateBottomPadding() + 32.dp)
                    .fillMaxWidth(0.8f),
                enabled = !uiState.isSubmitting,
                onClick = onSubmitClicked
            )
        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun PostSelectScreenSystemUIPreview() {
    LetterBLETheme {
        Scaffold { innerPadding ->
            PostSelectScreenContent(
                uiState = PostSelectUiState(
                    posts = listOf(Post("1", "Tokyo Station", 35.681236, 139.767125))
                ),
                hasFineLocationPermission = true,
                onLocationPermissionRequest = {},
                onPostClicked = {},
                onBackClicked = {},
                onRetryPostsClicked = {},
                onSubmitClicked = {},
                innerPadding = innerPadding
            )
        }
    }
}
@Composable
private fun PostSelectStatusContent(
    message: String,
    isError: Boolean,
    buttonText: String? = null,
    onButtonClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.92f))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isError) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
        if (buttonText != null && onButtonClick != null) {
            CommonButton(
                text = buttonText,
                modifier = Modifier.padding(top = 16.dp),
                onClick = onButtonClick
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

