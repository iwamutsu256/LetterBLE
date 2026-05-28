/**
 * HomeScreen.kt
 *
 * ホーム画面の見た目を作るファイル。
 * 現在ユーザー名と、各画面へ進むボタンを表示する。
 */
package com.example.letterble.feature.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
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
import com.example.letterble.R
import com.example.letterble.di.AppContainer
import com.example.letterble.ui.components.CommonButton
import com.example.letterble.ui.theme.LetterBLETheme

/**
 * ホーム画面を表示する Composable。
 */
@Composable
fun HomeScreen(
    appContainer: AppContainer,
    onReceivedClicked: () -> Unit,
    onCarryClicked: () -> Unit,
    onCreateLetterClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(
            userRepository = appContainer.userRepository,
            letterRepository = appContainer.letterRepository
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

    HomeScreenContent(
        currentUserName = uiState.currentUserName,
        isReceivedStatusLoading = uiState.isReceivedStatusLoading,
        receivedStatusErrorMessage = uiState.receivedStatusErrorMessage,
        hasReceivedLetters = uiState.hasReceivedLetters,
        receivedLetterCount = uiState.receivedLetterCount,
        onReceivedClicked = viewModel::onReceivedClicked,
        onHomeClicked = {},
        onCarryClicked = viewModel::onCarryClicked,
        onCreateLetterClicked = viewModel::onCreateLetterClicked,
        modifier = modifier
    )
}

@Composable
fun HomeScreenContent(
    currentUserName: String,
    isReceivedStatusLoading: Boolean,
    receivedStatusErrorMessage: String?,
    hasReceivedLetters: Boolean,
    receivedLetterCount: Int,
    onReceivedClicked: () -> Unit,
    onHomeClicked: () -> Unit,
    onCarryClicked: () -> Unit,
    onCreateLetterClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFFFFFA))
    ) {
        Image(
            painter = painterResource(id = R.drawable.icon),
            contentDescription = null,
            modifier = Modifier
                .size(110.dp)
                .padding(20.dp)
                .offset(y = 5.dp)
        )

        if (currentUserName.isNotBlank()) {
            Text(
                text = currentUserName,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 88.dp, top = 28.dp),
                color = Color(0xFF55433F)
            )
        }

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
                .width(150.dp)
                .height(150.dp)
                .align(Alignment.TopStart)
                .offset(x = 230.dp, y = 590.dp),
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
                .padding(bottom = 40.dp),
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
                color = Color(0xFF55433F)
            )

            Row(
                modifier = Modifier.padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CommonButton(
                    text = "受信",
                    modifier = Modifier.width(100.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0F0F6D),
                        contentColor = Color(0xFFFFFFFA)
                    ),
                    onClick = onReceivedClicked
                )
                CommonButton(
                    text = "ホーム",
                    modifier = Modifier.width(100.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0F0F6D),
                        contentColor = Color(0xFFFFFFFA)
                    ),
                    onClick = onHomeClicked
                )
                CommonButton(
                    text = "配達",
                    modifier = Modifier.width(100.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0F0F6D),
                        contentColor = Color(0xFFFFFFFA)
                    ),
                    onClick = onCarryClicked
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenContentPreview() {
    LetterBLETheme {
        HomeScreenContent(
            currentUserName = "sample-user",
            isReceivedStatusLoading = false,
            receivedStatusErrorMessage = null,
            hasReceivedLetters = true,
            receivedLetterCount = 2,
            onReceivedClicked = {},
            onHomeClicked = {},
            onCarryClicked = {},
            onCreateLetterClicked = {}
        )
    }
}
