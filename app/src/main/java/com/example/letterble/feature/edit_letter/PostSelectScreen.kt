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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Button
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.letterble.R
import com.example.letterble.di.AppContainer
import com.example.letterble.domain.model.Letter
import com.example.letterble.domain.model.Post
import com.example.letterble.ui.components.CommonBackButton
import com.example.letterble.ui.components.CommonButton
import com.example.letterble.ui.components.LetterMapView
import com.example.letterble.ui.theme.LetterBLEFontFamilies
import com.example.letterble.ui.theme.LetterBLEFontSize
import com.example.letterble.ui.theme.LetterBLEFontSize.Button
import com.example.letterble.ui.theme.LetterBLETextStyles
import com.example.letterble.ui.theme.LetterBLETheme
import com.example.letterble.ui.theme.LetterBleFontFamily
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlin.math.PI
import kotlin.math.ln
import kotlin.math.log2
import kotlin.math.min
import kotlin.math.sin

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

    BackHandler(enabled = uiState.isSubmitting || uiState.isSubmitted) {
        if (uiState.isSubmitted && !uiState.isSubmitting) {
            viewModel.onSubmittedOkClicked()
        }
        // 投函中(isSubmitting)は戻る操作を無視し、完了後はOKボタンと同じ挙動(Homeへ)にする。
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
                    Text("投函")
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
            onMapClicked = viewModel::onMapClicked,
            onBackClicked = onBackClicked,
            onRetryPostsClicked = viewModel::loadNearbyPosts,
            onSubmitClicked = viewModel::onPostSubmitClicked,
            onSubmittedOkClicked = viewModel::onSubmittedOkClicked,
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
    onMapClicked: () -> Unit,
    onBackClicked: () -> Unit,
    onRetryPostsClicked: () -> Unit,
    onSubmitClicked: () -> Unit,
    onSubmittedOkClicked: () -> Unit,
    innerPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    if (uiState.isSubmitted) {
        PostSubmittedContent(
            isSubmitting = uiState.isSubmitting,
            onOkClicked = onSubmittedOkClicked,
            innerPadding = innerPadding,
            modifier = modifier
        )
    } else {
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

                    uiState.currentLatLng() != null -> {
                        PostSelectMap(
                            posts = uiState.posts,
                            currentPosition = uiState.currentLatLng(),
                            selectedPost = uiState.selectedPost,
                            isPostSearchLoading = uiState.isPostSearchLoading,
                            hasFineLocationPermission = hasFineLocationPermission,
                            onPostClicked = onPostClicked,
                            onMapClicked = onMapClicked,
                            modifier = Modifier
                                .fillMaxSize()
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
                    .offset((-24).dp, 14.dp)
            )
            if (errorMessage != null && !uiState.canRetryPostSearch && hasFineLocationPermission) {
                PostSelectStatusContent(
                    message = errorMessage,
                    isError = true,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            if (
                errorMessage != null &&
                uiState.canRetryPostSearch &&
                hasFineLocationPermission &&
                uiState.currentLatLng() != null
            ) {
                PostSelectStatusContent(
                    message = errorMessage,
                    isError = true,
                    buttonText = "再試行",
                    modifier = Modifier.align(Alignment.Center),
                    onButtonClick = onRetryPostsClicked
                )
            }
            if (message != null && uiState.currentLatLng() != null && !uiState.isPostSearchLoading) {
                PostSelectStatusContent(
                    message = message,
                    isError = false,
                    buttonText = "再検索",
                    modifier = Modifier.align(Alignment.Center),
                    onButtonClick = onRetryPostsClicked
                )
            }
            if (uiState.selectedPost != null) {
                SelectedPostBottomSheet(
                    post = uiState.selectedPost,
                    enabled = !uiState.isSubmitting,
                    onSubmitClicked = onSubmitClicked,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(
                            start = 16.dp,
                            end = 16.dp,
                            bottom = innerPadding.calculateBottomPadding() + 16.dp
                        )
                )
            }
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
                onMapClicked = {},
                onBackClicked = {},
                onRetryPostsClicked = {},
                onSubmitClicked = {},
                onSubmittedOkClicked = {},
                innerPadding = innerPadding
            )
        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun PostSubmittedScreenSystemUIPreview() {
    LetterBLETheme {
        Scaffold { innerPadding ->
            PostSelectScreenContent(
                uiState = PostSelectUiState(
                    isSubmitted = true,
                    isSubmitting = false
                ),
                hasFineLocationPermission = true,
                onLocationPermissionRequest = {},
                onPostClicked = {},
                onMapClicked = {},
                onBackClicked = {},
                onRetryPostsClicked = {},
                onSubmitClicked = {},
                onSubmittedOkClicked = {},
                innerPadding = innerPadding
            )
        }
    }
}

@Composable
private fun PostSubmittedContent(
    isSubmitting: Boolean,
    onOkClicked: () -> Unit,
    innerPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Image(
            painter = painterResource(id = R.drawable.img08),
            contentDescription = null,
            modifier = Modifier
                .size(400.dp)
                .align(Alignment.TopEnd)
                .offset(110.dp, 20.dp)
        )
        Image(
            painter = painterResource(id = R.drawable.img09),
            contentDescription = null,
            modifier = Modifier
                .size(400.dp)
                .align(Alignment.BottomStart)
                .offset((-80).dp, 130.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .offset(y = 80.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Text(
                    text = "とうかんしました！",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = LetterBLEFontFamilies.NotoSansJp,
                        fontWeight = FontWeight.Black,
                        fontSize = LetterBLEFontSize.SectionTitle
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                Button(
                    modifier = Modifier
                        .padding(top = 40.dp)
                        .width(180.dp),
                    onClick = onOkClicked
                ) {
                    Text(
                        text = "OK!",
                        style = LetterBLETextStyles.EnglishButton.copy(
                            fontSize = LetterBLEFontSize.Button
                        )
                    )
                }
            }
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
    isPostSearchLoading: Boolean,
    hasFineLocationPermission: Boolean,
    onPostClicked: (Post) -> Unit,
    onMapClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val initialCenter = currentPosition ?: posts.firstOrNull()?.toLatLng() ?: DefaultPostMapCenter
    BoxWithConstraints(
        modifier = modifier.height(420.dp),
        contentAlignment = Alignment.Center
    ) {
        val density = LocalDensity.current
        val mapWidthPx = with(density) { maxWidth.toPx() }
        val mapHeightPx = with(density) { maxHeight.toPx() }
        val boundsPaddingPx = with(density) { PostMapBoundsPadding.toPx() }
        val initialZoom = currentPosition?.toRadiusZoom(
            mapWidthPx = mapWidthPx,
            mapHeightPx = mapHeightPx,
            paddingPx = boundsPaddingPx,
            radiusMeters = PostSearchRadiusMeters
        ) ?: 15f
        val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(initialCenter, initialZoom)
        }
        var isMapLoaded by remember { mutableStateOf(false) }
        var radiusCameraCenter by remember { mutableStateOf(initialCenter) }

        LaunchedEffect(isMapLoaded, currentPosition, posts) {
            if (!isMapLoaded || currentPosition == null || currentPosition == radiusCameraCenter) {
                return@LaunchedEffect
            }

            radiusCameraCenter = currentPosition
            cameraPositionState.move(
                CameraUpdateFactory.newLatLngZoom(
                    currentPosition,
                    currentPosition.toRadiusZoom(
                        mapWidthPx = mapWidthPx,
                        mapHeightPx = mapHeightPx,
                        paddingPx = boundsPaddingPx,
                        radiusMeters = PostSearchRadiusMeters
                    )
                )
            )
        }

        LaunchedEffect(isMapLoaded, selectedPost?.id) {
            if (!isMapLoaded || selectedPost == null) {
                return@LaunchedEffect
            }

            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLng(selectedPost.toLatLng()),
                durationMs = SelectedPostCameraAnimationMillis
            )
        }

        LetterMapView(
            modifier = Modifier.matchParentSize(),
            initialCenter = initialCenter,
            initialZoom = initialZoom,
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = hasFineLocationPermission),
            onMapClick = { onMapClicked() },
            onMapLoaded = { isMapLoaded = true }
        ) {
            val (selectedPosts, unselectedPosts) = posts.partition { post ->
                post.id == selectedPost?.id
            }
            (unselectedPosts + selectedPosts).forEach { post ->
                val isSelected = post.id == selectedPost?.id
                Marker(
                    state = MarkerState(position = post.toLatLng()),
                    title = post.name,
                    snippet = post.description.ifBlank { "${post.latitude}, ${post.longitude}" },
                    icon = BitmapDescriptorFactory.defaultMarker(
                        if (isSelected) BitmapDescriptorFactory.HUE_RED else BitmapDescriptorFactory.HUE_AZURE
                    ),
                    zIndex = if (isSelected) SelectedPostMarkerZIndex else DefaultPostMarkerZIndex,
                    onClick = {
                        onPostClicked(post)
                        true
                    }
                )
            }
        }

        when {
            isPostSearchLoading -> {
                PostSelectStatusContent(
                    message = "近くのポストを検索しています",
                    isError = false,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            posts.isEmpty() -> {
                Text(
                    text = "地図に表示できるポストはありません",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun SelectedPostBottomSheet(
    post: Post,
    enabled: Boolean,
    onSubmitClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        tonalElevation = 6.dp,
        shadowElevation = 6.dp,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = post.name.ifBlank { "郵便ポスト" },
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = post.description.ifBlank { "${post.latitude}, ${post.longitude}" },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            CommonButton(
                text = "ここに投函する",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                enabled = enabled,
                onClick = onSubmitClicked
            )
        }
    }
}

private val DefaultPostMapCenter = LatLng(35.681236, 139.767125)
private const val PostSearchRadiusMeters = 1_000.0
private val PostMapBoundsPadding = 160.dp
private const val SelectedPostCameraAnimationMillis = 500
private const val DefaultPostMarkerZIndex = 0f
private const val SelectedPostMarkerZIndex = 1f

private fun PostSelectUiState.currentLatLng(): LatLng? {
    val latitude = currentLatitude ?: return null
    val longitude = currentLongitude ?: return null
    return LatLng(latitude, longitude)
}

private fun Post.toLatLng(): LatLng {
    return LatLng(latitude, longitude)
}

private fun LatLng.toRadiusZoom(
    mapWidthPx: Float,
    mapHeightPx: Float,
    paddingPx: Float,
    radiusMeters: Double
): Float {
    val latitudeDelta = radiusMeters / MetersPerLatitudeDegree
    val longitudeDelta = radiusMeters / (MetersPerLatitudeDegree * kotlin.math.cos(Math.toRadians(latitude)))
    val south = latitude - latitudeDelta
    val north = latitude + latitudeDelta
    val west = longitude - longitudeDelta
    val east = longitude + longitudeDelta
    val usableWidth = (mapWidthPx - paddingPx * 2).coerceAtLeast(MinMapUsableSizePx)
    val usableHeight = (mapHeightPx - paddingPx * 2).coerceAtLeast(MinMapUsableSizePx)
    val latFraction = (mercatorLatitude(north) - mercatorLatitude(south)).coerceAtLeast(MinMapFraction)
    val lngFraction = ((east - west) / 360.0).coerceAtLeast(MinMapFraction)
    val latZoom = log2(usableHeight / WorldTileSizePx / latFraction).toFloat()
    val lngZoom = log2(usableWidth / WorldTileSizePx / lngFraction).toFloat()
    return min(latZoom, lngZoom).coerceIn(MinPostMapZoom, MaxPostMapZoom)
}

private fun mercatorLatitude(latitude: Double): Double {
    val sinLatitude = sin(Math.toRadians(latitude.coerceIn(-85.0, 85.0)))
    return 0.5 - ln((1 + sinLatitude) / (1 - sinLatitude)) / (4 * PI)
}

private const val MetersPerLatitudeDegree = 111_320.0
private const val WorldTileSizePx = 256.0
private const val MinMapUsableSizePx = 1f
private const val MinMapFraction = 1.0e-9
private const val MinPostMapZoom = 3f
private const val MaxPostMapZoom = 18f

