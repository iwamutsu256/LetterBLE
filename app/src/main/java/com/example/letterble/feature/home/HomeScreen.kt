/**
 * HomeScreen.kt
 *
 * 役割:
 * - ホーム画面のナビゲーションボタンを表示する
 * - ViewModelのイベントを受け取り画面遷移コールバックを呼ぶ
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.letterble.R
import com.example.letterble.ui.components.CommonButton
import com.example.letterble.ui.theme.LetterBLETheme

/**
 * ホーム画面を表示する。
 *
 * @param onReceivedClicked 受信一覧画面へ遷移するときに呼ぶコールバック
 * @param onCarryClicked 運搬中一覧画面へ遷移するときに呼ぶコールバック
 * @param onCreateLetterClicked 手紙作成画面へ遷移するときに呼ぶコールバック
 * @param modifier 画面全体に適用するModifier
 * @param viewModel ホーム画面のイベントを管理するViewModel
 */
@Composable
fun HomeScreen(
    onReceivedClicked: () -> Unit,
    onCarryClicked: () -> Unit,
    onCreateLetterClicked: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel()
) {
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
        onReceivedClicked = viewModel::onReceivedClicked,
        onCarryClicked = viewModel::onCarryClicked,
        onCreateLetterClicked = viewModel::onCreateLetterClicked,
        onStartClicked = { /* 手紙制作の処理 */ },
        modifier = modifier
    )

}

@Composable
fun HomeScreenContent(
    onReceivedClicked: () -> Unit,
    onCarryClicked: () -> Unit,
    onCreateLetterClicked: () -> Unit,
    onStartClicked: () -> Unit,
    modifier: Modifier = Modifier,
)
 {
    Box(
        modifier = Modifier
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
                .offset(x = 230.dp , y = 590.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = Color.Unspecified
            ),
            onClick = onStartClicked
        ) {
            Image(
                painter = painterResource(id = R.drawable.write),
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
        }
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(bottom = 40.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CommonButton(
                    text = "受信",
                    modifier = Modifier.width(100.dp),
                    onClick = onReceivedClicked
                )
                CommonButton(
                    text = "ホーム",
                    modifier = Modifier.width(100.dp),
                    onClick = onCarryClicked
                )
                CommonButton(
                    text = "配達",
                    modifier = Modifier.width(100.dp),
                    onClick = onCreateLetterClicked
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
            onReceivedClicked = {},
            onCarryClicked = {},
            onCreateLetterClicked = {},
            onStartClicked = {},
            modifier = Modifier
        )
    }
}

