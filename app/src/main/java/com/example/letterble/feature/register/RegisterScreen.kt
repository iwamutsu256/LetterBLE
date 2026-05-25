/**
 * RegisterScreen.kt
 *
 * 役割:
 * - ユーザー登録UIを表示する
 * - ユーザー入力を受け付ける
 *
 * 注意:
 * - 状態はViewModelから取得する
 * - Repositoryへ直接アクセスしない
 */
package com.example.letterble.feature.register

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.letterble.ui.theme.LetterBLETheme

// TODO: TextFieldでユーザー名入力を受け付ける
// TODO: RegisterViewModelのstateをcollectしてUIに反映する
// TODO: ボタンでRegisterViewModelの登録イベントを呼び出す
// TODO: 位置情報・Bluetoothの権限要求UIを表示する

/**
 * RegisterScreen.kt内だけで使うサブ画面。
 *
 * 画面全体の目的とVMが同じ場合は、NavGraphに別routeを増やさず、
 * 1つのScreen.kt内でこのようなサブ画面単位に分ける。
 *
 * 例:
 * - Start: 登録前の開始画面
 * - NewRegistration: ユーザー名入力や権限確認を行う新規登録画面
 */
private enum class RegisterSubScreen {
    Start,
    NewRegistration
}

/**
 * ユーザー登録画面の最小UIを表示する。
 *
 * @param onRegistered 登録完了後にホーム画面へ進むためのコールバック
 * @param modifier 画面全体に適用するModifier
 */
@Composable
fun RegisterScreen(
    onRegistered: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentScreen by rememberSaveable { mutableStateOf(RegisterSubScreen.Start) }

    // VMはRegisterScreenで1つだけ持つ想定。
    // ScreenContent以下は状態とイベントを受け取り、表示の分岐だけを担当する。
    RegisterScreenContent(
        currentScreen = currentScreen,
        onStartClicked = { currentScreen = RegisterSubScreen.NewRegistration },
        onRegistered = onRegistered,
        modifier = modifier
    )
}

@Composable
private fun RegisterScreenContent(
    currentScreen: RegisterSubScreen,
    onStartClicked: () -> Unit,
    onRegistered: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (currentScreen) {
        RegisterSubScreen.Start -> RegisterStartContent(
            onStartClicked = onStartClicked,
            modifier = modifier
        )

        RegisterSubScreen.NewRegistration -> NewRegistrationContent(
            onRegistered = onRegistered,
            modifier = modifier
        )
    }
}

@Composable
private fun RegisterStartContent(
    onStartClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "ユーザー登録",
            style = MaterialTheme.typography.headlineMedium
        )
        Button(
            modifier = Modifier.padding(top = 24.dp),
            onClick = onStartClicked
        ) {
            Text("登録して開始")
        }
    }
}

@Composable
private fun NewRegistrationContent(
    onRegistered: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "新規登録",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            modifier = Modifier.padding(top = 12.dp),
            text = "ユーザー情報を登録します"
        )
        Button(
            modifier = Modifier.padding(top = 24.dp),
            onClick = onRegistered
        ) {
            Text("登録完了")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RegisterStartContentPreview() {
    LetterBLETheme {
        RegisterStartContent(onStartClicked = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun NewRegistrationContentPreview() {
    LetterBLETheme {
        NewRegistrationContent(onRegistered = {})
    }
}
