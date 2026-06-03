/**
 * ReceivedMapDetailScreen.kt
 *
 * 役割:
 * - 経路地図を全画面で表示する
 * - ピンのタップなどで詳細（ユーザー名など）を確認できる
 */
package com.example.letterble.feature.received

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.letterble.di.AppContainer
import com.example.letterble.ui.components.CommonBackButton
import com.example.letterble.ui.components.LetterTreeMapView
import com.example.letterble.ui.theme.LetterBLETheme

/**
 * 経路地図の詳細画面（全画面表示）。
 *
 * @param appContainer Repository を ViewModel に渡すための依存関係入口
 * @param letterId 経路を表示する手紙ID
 * @param onBackClicked 前の画面へ戻るためのコールバック
 */
@Composable
fun ReceivedMapDetailScreen(
    appContainer: AppContainer,
    letterId: String,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: ReceivedViewModel = viewModel(factory = appContainer.receivedViewModelFactory)
    val uiState by viewModel.uiState.collectAsState()
    val detailState = uiState.detailState

    LaunchedEffect(letterId) {
        viewModel.loadLetterDetail(letterId)
    }

    Scaffold { innerPadding ->
        ReceivedMapDetailContent(
            detailState = detailState,
            onBackClicked = onBackClicked,
            innerPadding = innerPadding,
            modifier = modifier
        )
    }
}

@Composable
private fun ReceivedMapDetailContent(
    detailState: ReceivedDetailUiState,
    onBackClicked: () -> Unit,
    innerPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        when {
            detailState.isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            detailState.errorMessage != null -> {
                Text(
                    text = detailState.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center).padding(24.dp)
                )
            }
            detailState.detail != null -> {
                val detail = detailState.detail
                val routeHighlight = remember(detail.letter.fromUser, detail.letter.toUser, detail.tree) {
                    detail.tree.shortestDirectedRouteHighlight(
                        fromUser = detail.letter.fromUser,
                        toUser = detail.letter.toUser
                    )
                }

                LetterTreeMapView(
                    tree = detail.tree,
                    highlightedNodeIds = routeHighlight.nodeIds,
                    highlightedEdges = routeHighlight.edges,
                    showEdgeArrows = true,
                    markerMinimumZoom = ReceivedMapDetailMarkerMinimumZoom,
                    alwaysVisibleMarkerNodeIds = routeHighlight.endpointNodeIds,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        CommonBackButton(
            modifier = Modifier.padding(top = innerPadding.calculateTopPadding()),
            onClick = onBackClicked
        )
    }
}

private const val ReceivedMapDetailMarkerMinimumZoom = 7f
