/**
 * RegisterScreen.kt
 *
 * ユーザー登録画面の見た目を作るファイル。
 * 保存処理そのものは RegisterViewModel に任せる。
 */

// このファイルが登録画面 feature の置き場所にあることを示す。
package com.example.letterble.feature.register

// Column の中身を中央寄せするために使う。
import androidx.compose.foundation.layout.Arrangement
// 縦方向に UI を並べるために使う。
import androidx.compose.foundation.layout.Column
// 画面全体に広げるために使う。
import androidx.compose.foundation.layout.fillMaxSize
// 余白をつけるために使う。
import androidx.compose.foundation.layout.padding
// キーボードの完了ボタンなどを設定するために使う。
import androidx.compose.foundation.text.KeyboardOptions
// 保存中のぐるぐる表示に使う。
import androidx.compose.material3.CircularProgressIndicator
// アプリの文字サイズや色などのテーマを使う。
import androidx.compose.material3.MaterialTheme
// 枠付きの入力欄を表示するために使う。
import androidx.compose.material3.OutlinedTextField
// 文字を表示するために使う。
import androidx.compose.material3.Text
// Composable 関数を作るために使う。
import androidx.compose.runtime.Composable
// 状態変化に反応して一度だけ処理を動かすために使う。
import androidx.compose.runtime.LaunchedEffect
// StateFlow を Compose の State として受け取るために使う。
import androidx.compose.runtime.collectAsState
// by uiState の形で State を読みやすくするために使う。
import androidx.compose.runtime.getValue
// UI の配置位置を指定するために使う。
import androidx.compose.ui.Alignment
// UI に余白やサイズなどを指定するために使う。
import androidx.compose.ui.Modifier
// ViewModelFactory に渡す Context を取得するために使う。
import androidx.compose.ui.platform.LocalContext
// キーボードの IME アクションを指定するために使う。
import androidx.compose.ui.text.input.ImeAction
// dp 単位の余白を指定するために使う。
import androidx.compose.ui.unit.dp
// Compose で ViewModel を取得するために使う。
import androidx.lifecycle.viewmodel.compose.viewModel
// アプリ共通のボタン。
import com.example.letterble.ui.components.CommonButton

/**
 * ユーザー登録画面を表示する Composable。
 */
@Composable
fun RegisterScreen(
    // 登録完了後に Home へ進むため、AppNavGraph から渡される処理。
    onRegistered: () -> Unit,
    // 外側から画面全体の Modifier を渡せるようにする。
    modifier: Modifier = Modifier
) {
    // Factory で ViewModel を作るため、現在の Context を取得する。
    val context = LocalContext.current

    // RegisterViewModelFactory を使って、Repository 付きの RegisterViewModel を作る。
    val viewModel: RegisterViewModel = viewModel(
        // Context を渡して Factory を作る。
        factory = RegisterViewModelFactory(context)
    )

    // ViewModel の uiState を Compose 画面で読める形に変換する。
    val uiState by viewModel.uiState.collectAsState()

    // isRegistered が変わったときに実行される処理。
    LaunchedEffect(uiState.isRegistered) {
        // 登録済みになったら Home へ進む処理を呼ぶ。
        if (uiState.isRegistered) {
            // AppNavGraph 側で Home への navigate が実行される。
            onRegistered()
        }
    }

    // 画面全体を縦方向のレイアウトで作る。
    Column(
        // 外から受け取った modifier にサイズと余白を追加する。
        modifier = modifier
            // 画面いっぱいに広げる。
            .fillMaxSize()
            // 画面端から 24dp の余白をつける。
            .padding(24.dp),
        // 中身を縦方向の中央に置く。
        verticalArrangement = Arrangement.Center,
        // 中身を横方向の中央に置く。
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 画面タイトルを表示する。
        Text(
            // タイトル文字。
            text = "ユーザー登録",
            // 大きめの見出しスタイルを使う。
            style = MaterialTheme.typography.headlineMedium
        )

        // ユーザー名入力欄を表示する。
        OutlinedTextField(
            // 入力欄に表示する文字は uiState.userName。
            value = uiState.userName,
            // 入力が変わったら ViewModel に伝える。
            onValueChange = viewModel::onNameChanged,
            // タイトルとの間に余白をつける。
            modifier = Modifier.padding(top = 24.dp),
            // 入力欄のラベルを表示する。
            label = {
                // ラベル文字。
                Text("ユーザー名")
            },
            // ユーザー名は1行入力にする。
            singleLine = true,
            // 保存中は入力できないようにする。
            enabled = !uiState.isLoading,
            // キーボードの完了ボタンを Done にする。
            keyboardOptions = KeyboardOptions(
                // キーボード右下のボタンを「完了」にする。
                imeAction = ImeAction.Done
            )
        )

        // nullable な errorMessage を安全に扱うため、一度ローカル変数に入れる。
        val errorMessage = uiState.errorMessage

        // エラーメッセージがあるときだけ表示する。
        if (errorMessage != null) {
            // エラー文を表示する。
            Text(
                // 表示するエラー文。
                text = errorMessage,
                // 入力欄との間に余白をつける。
                modifier = Modifier.padding(top = 8.dp),
                // エラー色で表示する。
                color = MaterialTheme.colorScheme.error,
                // 小さめの本文スタイルを使う。
                style = MaterialTheme.typography.bodySmall
            )
        }

        // 登録開始ボタンを表示する。
        CommonButton(
            // ボタンに表示する文字。
            text = "開始",
            // 入力欄との間に余白をつける。
            modifier = Modifier.padding(top = 24.dp),
            // 保存中は連打できないように無効化する。
            enabled = !uiState.isLoading,
            // 押されたら ViewModel の登録処理を呼ぶ。
            onClick = viewModel::onNameSubmitClicked
        )

        // 保存中だけローディング表示を出す。
        if (uiState.isLoading) {
            // Firestore 保存中であることを示すぐるぐる表示。
            CircularProgressIndicator(
                // ボタンとの間に余白をつける。
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}
