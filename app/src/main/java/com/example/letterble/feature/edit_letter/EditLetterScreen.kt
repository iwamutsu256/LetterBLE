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

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.letterble.di.AppContainer
import com.example.letterble.ui.components.CommonBackButton
import com.example.letterble.ui.theme.LetterBLEColors
import com.example.letterble.ui.theme.LetterBLETheme
import com.example.letterble.ui.theme.NotoSansJpFontFamily
import kotlinx.coroutines.launch

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
    modifier: Modifier = Modifier,
    onSubmitClicked: () -> Unit = {},
    viewModel: EditLetterViewModel = viewModel(
        factory = appContainer.editLetterViewModelFactory()
    )
) {
    val view = LocalView.current
    val uiState by viewModel.uiState.collectAsState()
    var showBackConfirmDialog by remember { mutableStateOf(false) }

    DisposableEffect(view) {
        val window = view.context.findActivity()?.window
        val previousSoftInputMode = window?.attributes?.softInputMode
        window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)

        onDispose {
            if (window != null && previousSoftInputMode != null) {
                window.setSoftInputMode(previousSoftInputMode)
            }
        }
    }

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
            onSubmitClicked = viewModel::onSubmitClicked,
            onBackClicked = ::requestBack,
            innerPadding = innerPadding,
            modifier = modifier
        )
    }
}

/**
 * 手紙風の背景を表示するコンポーネント。
 */
@Composable
private fun LetterPaper(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        color = Color(0xFFF9F6EF),
        shadowElevation = 2.dp,
        shape = RoundedCornerShape(4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // 背景の点線枠。matchParentSize を使うことで、Surface の大きさに完全に追従する
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .padding(12.dp)
                    .drawBehind {
                        val stroke = Stroke(
                            width = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )
                        drawRect(
                            color = Color.Gray.copy(alpha = 0.3f),
                            style = stroke
                        )
                    }
            )

            // 実際のコンテンツ。
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
                    .padding(16.dp)
            ) {
                content()
            }
        }
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
    onSubmitClicked: () -> Unit,
    onBackClicked: () -> Unit,
    innerPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val toUserBringIntoViewRequester = remember { BringIntoViewRequester() }
    val sentenceBringIntoViewRequester = remember { BringIntoViewRequester() }
    var isSentenceFocused by remember { mutableStateOf(false) }
    var sentenceLineCount by remember { mutableIntStateOf(1) }

    LaunchedEffect(uiState.sentence, sentenceLineCount, isSentenceFocused) {
        if (isSentenceFocused) {
            sentenceBringIntoViewRequester.bringIntoView()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
        ) {
            val sentenceHeight = (sentenceLineCount * 32).dp
            val paperHeight = 180.dp + sentenceHeight

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(top = 72.dp, bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // 手紙本体
                LetterPaper(
                    modifier = Modifier.height(paperHeight)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // 宛先 (上辺に配置)
                        BasicTextField(
                            value = uiState.toUser,
                            onValueChange = onToUserChanged,
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = NotoSansJpFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = LetterBLEColors.TextPrimary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .bringIntoViewRequester(toUserBringIntoViewRequester)
                                .onFocusEvent { focusState ->
                                    if (focusState.isFocused) {
                                        coroutineScope.launch {
                                            toUserBringIntoViewRequester.bringIntoView()
                                        }
                                    }
                                },
                            singleLine = true,
                            decorationBox = { innerTextField ->
                                if (uiState.toUser.isEmpty()) {
                                    Text(
                                        "宛先",
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontFamily = NotoSansJpFontFamily,
                                            color = Color.Gray.copy(alpha = 0.5f),
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                                innerTextField()
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // 本文
                        BasicTextField(
                            value = uiState.sentence,
                            onValueChange = onSentenceChanged,
                            onTextLayout = { textLayoutResult ->
                                sentenceLineCount = maxOf(1, textLayoutResult.lineCount)
                            },
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = NotoSansJpFontFamily,
                                color = LetterBLEColors.TextPrimary,
                                lineHeight = 28.sp
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .bringIntoViewRequester(sentenceBringIntoViewRequester)
                                .onFocusEvent { focusState ->
                                    isSentenceFocused = focusState.isFocused
                                    if (focusState.isFocused) {
                                        coroutineScope.launch {
                                            sentenceBringIntoViewRequester.bringIntoView()
                                        }
                                    }
                                },
                            decorationBox = { innerTextField ->
                                if (uiState.sentence.isEmpty()) {
                                    Text(
                                        "手紙の内容を入力...",
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontFamily = NotoSansJpFontFamily,
                                            color = Color.Gray.copy(alpha = 0.5f)
                                        )
                                    )
                                }
                                innerTextField()
                            }
                        )

                        // 宛先と差出人の間の可変スペース
                        Spacer(modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.height(24.dp))

                        // 差出人 (下辺に配置)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(
                                text = "${uiState.sentence.length} / ${EditLetterViewModel.MAX_SENTENCE_LENGTH}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (uiState.sentence.length >= EditLetterViewModel.MAX_SENTENCE_LENGTH) Color.Red else Color.Gray
                                )
                            )
                            Text(
                                text = uiState.fromUser,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontFamily = NotoSansJpFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = LetterBLEColors.TextPrimary
                                )
                            )
                        }
                    }
                }

                // ポストを探すボタン
                Button(
                    onClick = onSubmitClicked,
                    modifier = Modifier
                        .padding(top = 40.dp)
                        .padding(horizontal = 24.dp)
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !uiState.isSubmitting && !uiState.isNavigatingToPostSelect,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LetterBLEColors.RegisterBackground,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "ポストを探す",
                            style = TextStyle(
                                fontFamily = NotoSansJpFontFamily,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black
                            )
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        // 戻るボタンをオーバーレイとして配置。スクロールやキーボードの影響を受けない。
        CommonBackButton(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = innerPadding.calculateTopPadding()),
            onClick = onBackClicked
        )

        // メッセージ表示
        uiState.message?.let { message ->
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp),
                color = Color.Black.copy(alpha = 0.7f),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text(
                    text = message,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
            }
        }
    }
}

private tailrec fun Context.findActivity(): Activity? {
    return when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}


@Preview(showSystemUi = true)
@Composable
private fun EditLetterScreenSystemUIPreview() {
    LetterBLETheme {
        Scaffold { innerPadding ->
            EditLetterScreenContent(
                uiState = EditLetterUiState(
                    toUser = "Bob",
                    fromUser = "Alice",
                    sentence = "This is a sample letter sentence for preview."
                ),
                onToUserChanged = {},
                onSentenceChanged = {},
                onSubmitClicked = {},
                onBackClicked = {},
                innerPadding = innerPadding
            )
        }
    }
}
