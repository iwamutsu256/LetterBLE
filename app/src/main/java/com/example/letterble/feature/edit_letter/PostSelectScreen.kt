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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
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
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(top = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.posts, key = { it.id }) { post ->
                        PostRow(
                            post = post,
                            selected = post.id == uiState.selectedPost?.id,
                            onClick = { viewModel.onPostSelected(post) }
                        )
                    }
                }
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
private fun PostRow(
    post: Post,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )
        Column(modifier = Modifier.padding(start = 8.dp)) {
            Text(
                text = post.name,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "${post.latitude}, ${post.longitude}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
