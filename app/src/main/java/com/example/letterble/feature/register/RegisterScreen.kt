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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.letterble.ui.components.CommonButton

// TODO: TextFieldでユーザー名入力を受け付ける
// TODO: RegisterViewModelのstateをcollectしてUIに反映する
// TODO: ボタンでRegisterViewModelの登録イベントを呼び出す
// TODO: 位置情報・Bluetoothの権限要求UIを表示する
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
    val context = LocalContext.current
    val viewModel: RegisterViewModel = viewModel(
        factory = RegisterViewModelFactory(context)
    )

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isRegistered) {
        if (uiState.isRegistered) {
            onRegistered()
        }
    }

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

        OutlinedTextField(
            value = uiState.userName,
            onValueChange = viewModel::onNameChanged,
            modifier = Modifier.padding(top = 24.dp),
            label = {
                Text("ユーザー名")
            },
            singleLine = true,
            enabled = !uiState.isLoading,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done
            )
        )

        val errorMessage = uiState.errorMessage
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                modifier = Modifier.padding(top = 8.dp),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        CommonButton(
            text = "開始",
            modifier = Modifier.padding(top = 24.dp),
            enabled = !uiState.isLoading,
            onClick = viewModel::onNameSubmitClicked
        )

        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}