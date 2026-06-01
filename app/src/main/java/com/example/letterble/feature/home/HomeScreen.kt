/**
 * HomeScreen.kt
 *
 * ホーム画面の見た目を作るファイル。
 * 現在ユーザー名と、各画面へ進むボタンを表示する。
 */
package com.example.letterble.feature.home

import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.letterble.R
import com.example.letterble.di.AppContainer
import com.example.letterble.ui.components.CommonBottomNavigation
import com.example.letterble.ui.theme.LetterBLEColors
import com.example.letterble.ui.theme.LetterBLETheme

/**
 * ホーム画面を表示する Composable。
 */
@Composable
fun HomeScreen(
    navController: NavHostController,
    appContainer: AppContainer,
    blePermissionErrorMessage: String?,
    onOpenAppSettingsClicked: () -> Unit,
    onRequestAddBleTileClicked: () -> Unit,
    onReceivedClicked: () -> Unit,
    onCarryClicked: () -> Unit,
    onCreateLetterClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(
            userRepository = appContainer.userRepository,
            letterRepository = appContainer.letterRepository,
            bleStatusRepository = appContainer.bleStatusRepository
        )
    )
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.navigationEvents.collect { event ->
            when (event) {
                HomeNavigationEvent.NavigateToReceived -> onReceivedClicked()
                HomeNavigationEvent.NavigateToCarry -> onCarryClicked()
                HomeNavigationEvent.NavigateToEditLetter -> onCreateLetterClicked()
            }
        }
    }

    CommonBottomNavigation(navController = navController) { innerPadding ->
        val canRequestAddBleTile = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
        HomeScreenContent(
            currentUserName = uiState.currentUserName,
            isReceivedStatusLoading = uiState.isReceivedStatusLoading,
            receivedStatusErrorMessage = uiState.receivedStatusErrorMessage,
            hasReceivedLetters = uiState.hasReceivedLetters,
            receivedLetterCount = uiState.receivedLetterCount,
            isBleEnabled = uiState.isBleEnabled,
            isBleRunning = uiState.isBleRunning,
            shouldShowBleTilePrompt = uiState.shouldShowBleTilePrompt && canRequestAddBleTile,
            blePermissionErrorMessage = blePermissionErrorMessage,
            onOpenAppSettingsClicked = onOpenAppSettingsClicked,
            onRequestAddBleTileClicked = {
                viewModel.onBleTilePromptHandled()
                onRequestAddBleTileClicked()
            },
            onDismissBleTilePrompt = viewModel::onBleTilePromptHandled,
            onReceivedClicked = viewModel::onReceivedClicked,
            onHomeClicked = {},
            onCarryClicked = viewModel::onCarryClicked,
            onCreateLetterClicked = viewModel::onCreateLetterClicked,
            modifier = modifier,
            innerPadding = innerPadding
        )
    }
}

@Composable
fun HomeScreenContent(
    currentUserName: String,
    isReceivedStatusLoading: Boolean,
    receivedStatusErrorMessage: String?,
    hasReceivedLetters: Boolean,
    receivedLetterCount: Int,
    isBleEnabled: Boolean,
    isBleRunning: Boolean,
    shouldShowBleTilePrompt: Boolean,
    blePermissionErrorMessage: String?,
    onOpenAppSettingsClicked: () -> Unit,
    onRequestAddBleTileClicked: () -> Unit,
    onDismissBleTilePrompt: () -> Unit,
    onReceivedClicked: () -> Unit,
    onHomeClicked: () -> Unit,
    onCarryClicked: () -> Unit,
    onCreateLetterClicked: () -> Unit,
    modifier: Modifier = Modifier,
    innerPadding: PaddingValues = PaddingValues()
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LetterBLEColors.AppBackground)
    ) {
        Image(
            painter = painterResource(id = R.drawable.icon),
            contentDescription = null,
            modifier = Modifier
                .size(110.dp)
                .padding(20.dp)
                .offset(x = 20.dp, y = 20.dp)
        )

        if (currentUserName.isNotBlank()) {
            Text(
                text = currentUserName,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .width(300.dp)
                    .padding(start = 88.dp, top = 28.dp)
                    .offset(x = 35.dp, y = 35.dp),
                color = LetterBLEColors.TextPrimary,
                style = MaterialTheme.typography.titleLarge
            )
        }

        BleStatusIndicator(
            isBleEnabled = isBleEnabled,
            isBleRunning = isBleRunning,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 38.dp, end = 24.dp)
        )

        Image(
            painter = painterResource(id = R.drawable.post),
            contentDescription = null,
            modifier = Modifier
                .size(400.dp)
                .align(Alignment.TopCenter)
                .offset(y = 200.dp)
        )

        Button(
            modifier = Modifier
                .width(130.dp)
                .height(130.dp)
                .offset(x = 250.dp, y = 590.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = Color.Unspecified
            ),
            onClick = onCreateLetterClicked
        ) {
            Image(
                painter = painterResource(id = R.drawable.write),
                contentDescription = "手紙を書く",
                modifier = Modifier.fillMaxSize()
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(bottom = innerPadding.calculateBottomPadding() + 16.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = when {
                    isReceivedStatusLoading -> "受信状況を確認中"
                    receivedStatusErrorMessage != null -> "受信状況を確認できません"
                    hasReceivedLetters -> "〒 受信した手紙 ${receivedLetterCount}件"
                    else -> "受信した手紙はありません"
                },
                color = LetterBLEColors.TextPrimary,
                style = MaterialTheme.typography.bodyLarge
            )
        }

        if (blePermissionErrorMessage != null) {
            AlertDialog(
                onDismissRequest = {},
                title = { Text("権限が必要です") },
                text = { Text(blePermissionErrorMessage) },
                confirmButton = {
                    TextButton(onClick = onOpenAppSettingsClicked) {
                        Text("設定を開く")
                    }
                }
            )
        }

        if (shouldShowBleTilePrompt && blePermissionErrorMessage == null) {
            AlertDialog(
                onDismissRequest = onDismissBleTilePrompt,
                title = { Text("BLE通信をタイルで切り替えますか？") },
                text = {
                    Text("クイック設定に Letter BLE を追加すると、画面上からいつでも通信のON/OFFを切り替えられます。")
                },
                confirmButton = {
                    TextButton(onClick = onRequestAddBleTileClicked) {
                        Text("追加する")
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismissBleTilePrompt) {
                        Text("あとで")
                    }
                }
            )
        }
    }
}

@Composable
private fun BleStatusIndicator(
    isBleEnabled: Boolean,
    isBleRunning: Boolean,
    modifier: Modifier = Modifier
) {
    val isActive = isBleEnabled && isBleRunning
    val label = if (isActive) "BLE ON" else "BLE OFF"
    val indicatorColor = if (isActive) Color(0xFF2E7D32) else Color(0xFF8A817C)

    Row(
        modifier = modifier
            .background(
                color = LetterBLEColors.AppBackground.copy(alpha = 0.9f),
                shape = CircleShape
            )
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(indicatorColor, CircleShape)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            color = LetterBLEColors.TextPrimary,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Preview(showSystemUi = true)
@Composable
private fun HomeScreenSystemUIPreview() {
    val navController = rememberNavController()

    LetterBLETheme {
        CommonBottomNavigation(navController = navController) { innerPadding ->
            HomeScreenContent(
                currentUserName = "sample-user",
                isReceivedStatusLoading = false,
                receivedStatusErrorMessage = null,
                hasReceivedLetters = true,
                receivedLetterCount = 2,
                isBleEnabled = true,
                isBleRunning = true,
                shouldShowBleTilePrompt = false,
                blePermissionErrorMessage = null,
                onOpenAppSettingsClicked = {},
                onRequestAddBleTileClicked = {},
                onDismissBleTilePrompt = {},
                onReceivedClicked = {},
                onHomeClicked = {},
                onCarryClicked = {},
                onCreateLetterClicked = {},
                modifier = Modifier,
                innerPadding = innerPadding
            )
        }
    }
}
