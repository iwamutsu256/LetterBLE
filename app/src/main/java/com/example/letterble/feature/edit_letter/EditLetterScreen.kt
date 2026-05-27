/**
 * EditLetterScreen.kt
 *
 * 役割:
 * - 手紙入力UIを表示する
 * - 手紙作成の状態を画面に反映する
 *
 * 注意:
 * - 手紙作成ロジックはViewModel / UseCaseへ委譲する
 */
package com.example.letterble.feature.edit_letter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun EditLetterScreen(
    viewModel: EditLetterViewModel,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    // 投函完了になったら自動で前の画面へ戻る。
    LaunchedEffect(uiState.isSubmitted) {
        if (uiState.isSubmitted) onBackClicked()
    }

    // 戻る確認ダイアログ。「保存して戻る」「保存せずに戻る」を提示する。
    if (uiState.showBackConfirmDialog) {
        AlertDialog(
            onDismissRequest = viewModel::onBackDialogDismissed,
            title = { Text("下書きを保存しますか？") },
            text = { Text("保存しないと入力内容が消えます。") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onSaveDraftClicked()
                    viewModel.onBackDialogDismissed()
                    onBackClicked()
                }) {
                    Text("保存して戻る")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.onDiscardAndBack()
                    onBackClicked()
                }) {
                    Text("保存せずに戻る")
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "手紙作成",
            style = MaterialTheme.typography.headlineMedium
        )

        // 宛先入力欄。
        OutlinedTextField(
            value = uiState.toName,
            onValueChange = viewModel::onToChanged,
            label = { Text("宛先") },
            modifier = Modifier.fillMaxWidth()
        )

        // 本文入力欄。
        OutlinedTextField(
            value = uiState.sentence,
            onValueChange = viewModel::onSentenceChanged,
            label = { Text("本文") },
            minLines = 5,
            modifier = Modifier.fillMaxWidth()
        )

        // 投函ボタン。送信中は押せないようにする。
        Button(
            onClick = viewModel::onSubmitClicked,
            enabled = !uiState.isSubmitting,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (uiState.isSubmitting) "投函中…" else "投函する（仮座標）")
        }

        // 下書き保存ボタン。
        TextButton(
            onClick = viewModel::onSaveDraftClicked,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("下書き保存")
        }

        // 戻るボタン。直接戻らず確認ダイアログを出す。
        TextButton(
            onClick = viewModel::onBackButtonClicked,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("戻る")
        }
    }
}