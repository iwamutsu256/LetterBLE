/**
 * HomeScreen.kt
 *
 * ホーム画面の見た目を作るファイル。
 * 現在ユーザー名と、各画面へ進むボタンを表示する。
 */

// このファイルがホーム画面 feature の置き場所にあることを示す。
package com.example.letterble.feature.home

// 中央寄せなどの配置指定に使う。
import androidx.compose.foundation.layout.Arrangement
// 左上表示と中央表示を重ねるために使う。
import androidx.compose.foundation.layout.Box
// 縦方向に UI を並べるために使う。
import androidx.compose.foundation.layout.Column
// 画面全体に広げるために使う。
import androidx.compose.foundation.layout.fillMaxSize
// 余白をつけるために使う。
import androidx.compose.foundation.layout.padding
// アプリの文字サイズや色などのテーマを使う。
import androidx.compose.material3.MaterialTheme
// 文字を表示するために使う。
import androidx.compose.material3.Text
// Composable 関数を作るために使う。
import androidx.compose.runtime.Composable
// ViewModel からの画面遷移イベントを受け取るために使う。
import androidx.compose.runtime.LaunchedEffect
// StateFlow を Compose の State として受け取るために使う。
import androidx.compose.runtime.collectAsState
// by uiState の形で State を読みやすくするために使う。
import androidx.compose.runtime.getValue
// UI の配置位置を指定するために使う。
import androidx.compose.ui.Alignment
// UI に余白やサイズなどを指定するために使う。
import androidx.compose.ui.Modifier
// dp 単位の余白を指定するために使う。
import androidx.compose.ui.unit.dp
// Compose で ViewModel を取得するために使う。
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.letterble.di.AppContainer
// アプリ共通のボタン。
import com.example.letterble.ui.components.CommonButton

/**
 * ホーム画面を表示する Composable。
 */
@Composable
fun HomeScreen(
    // AppContainer から ViewModel に必要な依存を受け取る。
    appContainer: AppContainer,
    // 受信一覧画面へ遷移するため、AppNavGraph から渡される処理。
    onReceivedClicked: () -> Unit,
    // 運搬中一覧画面へ遷移するため、AppNavGraph から渡される処理。
    onCarryClicked: () -> Unit,
    // 手紙作成画面へ遷移するため、AppNavGraph から渡される処理。
    onCreateLetterClicked: () -> Unit,
    // 外側から画面全体の Modifier を渡せるようにする。
    modifier: Modifier = Modifier
) {
    // AppContainer の Repository を渡して HomeViewModel を作る。
    val viewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(
            userRepository = appContainer.userRepository,
            letterRepository = appContainer.letterRepository
        )
    )

    // ViewModel の uiState を Compose 画面で読める形に変換する。
    val uiState by viewModel.uiState.collectAsState()

    // ViewModel から画面遷移イベントが流れてきたときに実行する。
    LaunchedEffect(viewModel) {
        // navigationEvents を監視する。
        viewModel.navigationEvents.collect { event ->
            // 流れてきたイベントの種類によって、AppNavGraph の遷移処理を呼び分ける。
            when (event) {
                // 受信一覧画面へ進む。
                HomeNavigationEvent.NavigateToReceived -> onReceivedClicked()
                // 運搬中一覧画面へ進む。
                HomeNavigationEvent.NavigateToCarry -> onCarryClicked()
                // 手紙作成画面へ進む。
                HomeNavigationEvent.NavigateToEditLetter -> onCreateLetterClicked()
            }
        }
    }

    // 左上のユーザー名と中央のメニューを同じ画面に重ねて配置する。
    Box(
        // 外から受け取った modifier にサイズと余白を追加する。
        modifier = modifier
            // 画面いっぱいに広げる。
            .fillMaxSize()
            // 画面端から 24dp の余白をつける。
            .padding(24.dp)
    ) {
        // 現在ユーザー名がある場合だけ表示する。
        if (uiState.currentUserName.isNotBlank()) {
            // 左上にユーザー名を表示する。
            Text(
                // 表示するユーザー名。
                text = uiState.currentUserName,
                // Box の左上に配置する。
                modifier = Modifier.align(Alignment.TopStart),
                // 通常本文サイズで表示する。
                style = MaterialTheme.typography.bodyMedium
            )
        }

        // ホーム画面の中央メニューを縦に並べる。
        Column(
            // Column 自体は画面いっぱいに広げる。
            modifier = Modifier.fillMaxSize(),
            // 中身を縦方向の中央に置く。
            verticalArrangement = Arrangement.Center,
            // 中身を横方向の中央に置く。
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ホーム画面のタイトル。
            Text(
                // タイトル文字。
                text = "ホーム",
                // 大きめの見出しスタイルを使う。
                style = MaterialTheme.typography.headlineMedium
            )

            Text(
                text = when {
                    uiState.isReceivedStatusLoading -> "受信状況を確認中"
                    uiState.receivedStatusErrorMessage != null -> "受信状況を確認できません"
                    uiState.hasReceivedLetters -> "〒 受信した手紙 ${uiState.receivedLetterCount}件"
                    else -> "受信した手紙はありません"
                },
                modifier = Modifier.padding(top = 16.dp),
                style = MaterialTheme.typography.bodyMedium
            )

            // 受信した手紙一覧へ進むボタン。
            CommonButton(
                // ボタンに表示する文字。
                text = if (uiState.hasReceivedLetters) {
                    "受信した手紙を見る"
                } else {
                    "受信した手紙"
                },
                // タイトルとの間に余白をつける。
                modifier = Modifier.padding(top = 16.dp),
                // 押されたら ViewModel にイベントを渡す。
                onClick = viewModel::onReceivedClicked
            )

            // 運搬中の手紙一覧へ進むボタン。
            CommonButton(
                // ボタンに表示する文字。
                text = "運搬中の手紙",
                // 上のボタンとの間に余白をつける。
                modifier = Modifier.padding(top = 8.dp),
                // 押されたら ViewModel にイベントを渡す。
                onClick = viewModel::onCarryClicked
            )

            // 手紙作成画面へ進むボタン。
            CommonButton(
                // ボタンに表示する文字。
                text = "手紙を書く",
                // 上のボタンとの間に余白をつける。
                modifier = Modifier.padding(top = 8.dp),
                // 押されたら ViewModel にイベントを渡す。
                onClick = viewModel::onCreateLetterClicked
            )
        }
    }
}
