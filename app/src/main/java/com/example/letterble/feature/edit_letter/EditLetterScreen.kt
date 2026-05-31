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

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.letterble.di.AppContainer
import com.example.letterble.ui.components.CommonButton

/**
 * 手紙作成画面の最小UIを表示する。
 *
 * @param onBackClicked 前の画面へ戻るためのコールバック
 * @param onSubmitClicked 投函へ進むときに呼ぶコールバック
 * @param modifier 画面全体に適用するModifier
 * @param viewModel 手紙作成状態を管理するViewModel
 */
@Composable
fun EditLetterScreen(
    // AppContainer から下書き保存に必要な依存を受け取る。
    appContainer: AppContainer,
    onBackClicked: () -> Unit,
    onSubmitClicked: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: EditLetterViewModel = viewModel(
        factory = appContainer.editLetterViewModelFactory()
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    var showBackConfirmDialog by remember { mutableStateOf(false) }

    fun requestBack() {
        if (uiState.hasInput) {
            showBackConfirmDialog = true
        } else {
            onBackClicked()
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                EditLetterEvent.NavigateToPostSelect -> {
                    onSubmitClicked()
                    viewModel.onPostSelectNavigationHandled()
                }
            }
        }
    }

    BackHandler {
        requestBack()
    }

    if (showBackConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showBackConfirmDialog = false },
            title = { Text("下書きの扱い") },
            text = { Text("入力中の内容を下書きとして保持しますか？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.onSaveDraftClicked()
                        showBackConfirmDialog = false
                        onBackClicked()
                    }
                ) {
                    Text("保持")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.onClearDraftClicked()
                        showBackConfirmDialog = false
                        onBackClicked()
                    }
                ) {
                    Text("削除")
                }
            }
        )
    }

    Scaffold { innerPadding ->
        EditLetterScreenContent(
            uiState = uiState,
            onToUserChanged = viewModel::onToUserChanged,
            onSentenceChanged = viewModel::onSentenceChanged,
            onSaveDraftClicked = viewModel::onSaveDraftClicked,
            onClearDraftClicked = viewModel::onClearDraftClicked,
            onSubmitClicked = viewModel::onSubmitClicked,
            onRequestBack = ::requestBack,
            innerPadding = innerPadding,
            modifier = modifier
        )
    }
}

/**
 * 表示ロジックを分離したコンテンツ部分。
 */
@Composable
private fun EditLetterScreenContent(
    uiState: EditLetterUiState,
    onToUserChanged: (String) -> Unit,
    onSentenceChanged: (String) -> Unit,
    onSaveDraftClicked: () -> Unit,
    onClearDraftClicked: () -> Unit,
    onSubmitClicked: () -> Unit,
    onRequestBack: () -> Unit,
    innerPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "手紙作成",
            style = MaterialTheme.typography.headlineMedium
        )

        OutlinedTextField(
            value = uiState.toUser,
            onValueChange = onToUserChanged,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
            label = { Text("宛先") },
            singleLine = true
        )

        OutlinedTextField(
            value = uiState.sentence,
            onValueChange = onSentenceChanged,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 160.dp)
                .padding(top = 12.dp),
            label = { Text("本文") },
            minLines = 6
        )

        uiState.message?.let { message ->
            Text(
                text = message,
                modifier = Modifier.padding(top = 12.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        CommonButton(
            text = "下書き保存",
            modifier = Modifier.padding(top = 24.dp),
            onClick = onSaveDraftClicked
        )
        CommonButton(
            text = "下書き削除",
            modifier = Modifier.padding(top = 8.dp),
            onClick = onClearDraftClicked
        )
        CommonButton(
            text = if (uiState.isNavigatingToPostSelect) "移動中" else "ポストを選ぶ",
            modifier = Modifier.padding(top = 8.dp),
            enabled = !uiState.isSubmitting && !uiState.isNavigatingToPostSelect,
            onClick = onSubmitClicked
        )
        CommonButton(
            text = "戻る",
            modifier = Modifier.padding(top = 8.dp),
            onClick = onRequestBack
        )
    }
}

@Preview(showSystemUi = true)
@Composable
private fun EditLetterScreenSystemUIPreview() {
    MaterialTheme {
        Scaffold { innerPadding ->
            EditLetterScreenContent(
                uiState = EditLetterUiState(
                    toUser = "Bob",
                    sentence = "This is a sample letter sentence for preview."
                ),
                onToUserChanged = {},
                onSentenceChanged = {},
                onSaveDraftClicked = {},
                onClearDraftClicked = {},
                onSubmitClicked = {},
                onRequestBack = {},
                innerPadding = innerPadding
            )
        }
    }
}
