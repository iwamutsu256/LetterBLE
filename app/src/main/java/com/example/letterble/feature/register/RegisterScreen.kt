/**
 * RegisterScreen.kt
 *
 * ユーザー登録画面の見た目を作るファイル。
 * 保存処理そのものは RegisterViewModel に任せる。
 */
package com.example.letterble.feature.register

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.letterble.R
import com.example.letterble.di.AppContainer
import com.example.letterble.ui.theme.LetterBLETheme

private enum class RegisterSubScreen {
    Start,
    NewRegistration
}

/**
 * ユーザー登録画面を表示する Composable。
 */
@Composable
fun RegisterScreen(
    appContainer: AppContainer,
    onRegistered: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: RegisterViewModel = viewModel(
        factory = RegisterViewModelFactory(appContainer.userRepository)
    )
    val uiState by viewModel.uiState.collectAsState()
    var currentScreen by rememberSaveable { mutableStateOf(RegisterSubScreen.Start) }

    LaunchedEffect(uiState.isRegistered) {
        if (uiState.isRegistered) {
            onRegistered()
        }
    }

    Scaffold { innerPadding ->
        RegisterScreenContent(
            currentScreen = currentScreen,
            userName = uiState.userName,
            errorMessage = uiState.errorMessage,
            isLoading = uiState.isLoading,
            onStartClicked = { currentScreen = RegisterSubScreen.NewRegistration },
            onNameChanged = viewModel::onNameChanged,
            onNameSubmitClicked = viewModel::onNameSubmitClicked,
            modifier = modifier,
            innerPadding = innerPadding
        )
    }
}

@Composable
private fun RegisterScreenContent(
    currentScreen: RegisterSubScreen,
    userName: String,
    errorMessage: String?,
    isLoading: Boolean,
    onStartClicked: () -> Unit,
    onNameChanged: (String) -> Unit,
    onNameSubmitClicked: () -> Unit,
    modifier: Modifier = Modifier,
    innerPadding: PaddingValues = PaddingValues()
) {
    when (currentScreen) {
        RegisterSubScreen.Start -> RegisterStartContent(
            onStartClicked = onStartClicked,
            modifier = modifier,
            innerPadding = innerPadding
        )

        RegisterSubScreen.NewRegistration -> NewRegistrationContent(
            userName = userName,
            errorMessage = errorMessage,
            isLoading = isLoading,
            onNameChanged = onNameChanged,
            onNameSubmitClicked = onNameSubmitClicked,
            modifier = modifier,
            innerPadding = innerPadding
        )
    }
}

@Composable
private fun RegisterStartContent(
    onStartClicked: () -> Unit,
    modifier: Modifier = Modifier,
    innerPadding: PaddingValues = PaddingValues()
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFFFFFA))
    ) {
        // 背景画像などはエッジまで広げる
        Image(
            painter = painterResource(id = R.drawable.img01),
            contentDescription = null,
            modifier = Modifier
                .size(320.dp)
                .offset(x = (-100).dp, y = (-100).dp)
        )
        Image(
            painter = painterResource(id = R.drawable.img02),
            contentDescription = null,
            modifier = Modifier
                .size(120.dp)
                .offset(x = 140.dp, y = 80.dp)
        )
        Image(
            painter = painterResource(id = R.drawable.img03),
            contentDescription = null,
            modifier = Modifier
                .size(350.dp)
                .offset(x = 100.dp, y = 650.dp)
        )
        Image(
            painter = painterResource(id = R.drawable.img04),
            contentDescription = null,
            modifier = Modifier
                .size(100.dp)
                .offset(x = 100.dp, y = 650.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Sheep relay",
                fontSize = 40.sp,
                style = MaterialTheme.typography.headlineMedium
            )
            Button(
                modifier = Modifier
                    .padding(top = 40.dp)
                    .width(150.dp)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF55433F),
                    contentColor = Color(0xFFFFFFFA)
                ),
                onClick = onStartClicked
            ) {
                Text(
                    text = "Log In",
                    fontSize = 20.sp
                )
            }
        }
    }
}

@Composable
private fun NewRegistrationContent(
    userName: String,
    errorMessage: String?,
    isLoading: Boolean,
    onNameChanged: (String) -> Unit,
    onNameSubmitClicked: () -> Unit,
    modifier: Modifier = Modifier,
    innerPadding: PaddingValues = PaddingValues()
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFF6242))
    ) {
        Image(
            painter = painterResource(id = R.drawable.img06),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(850.dp)
                .offset(x = 5.dp, y = (-350).dp)
                .graphicsLayer(rotationZ = -35f)
        )
        Text(
            text = "What's Your",
            fontSize = 50.sp,
            color = Color(0xFF0F0F6D),
            modifier = Modifier
                .padding(top = innerPadding.calculateTopPadding())
                .offset(x = 50.dp, y = 60.dp)
        )
        Text(
            text = "Name?",
            fontSize = 50.sp,
            color = Color(0xFF0F0F6D),
            modifier = Modifier
                .padding(top = innerPadding.calculateTopPadding())
                .offset(x = 190.dp, y = 120.dp)
                .padding(bottom = 90.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.icon),
                contentDescription = null,
                modifier = Modifier
                    .size(270.dp)
                    .padding(top = 20.dp, bottom = 20.dp)
            )
            OutlinedTextField(
                value = userName,
                onValueChange = onNameChanged,
                placeholder = { Text("名前を入力") },
                singleLine = true,
                enabled = !isLoading,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color(0xFF0F0F6D),
                    unfocusedIndicatorColor = Color(0xFF0F0F6D),
                    cursorColor = Color(0xFF04041F)
                ),
                modifier = Modifier
                    .width(265.dp)
                    .height(80.dp)
                    .padding(top = 24.dp)
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    modifier = Modifier.padding(top = 8.dp),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Button(
                modifier = Modifier
                    .padding(top = 40.dp)
                    .width(265.dp)
                    .height(50.dp),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0F0F6D),
                    contentColor = Color(0xFFFFF01D)
                ),
                onClick = onNameSubmitClicked
            ) {
                Text(
                    text = "次へ",
                    fontSize = 20.sp
                )
            }

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun RegisterStartScreenSystemUIPreview() {
    LetterBLETheme {
        Scaffold { innerPadding ->
            RegisterScreenContent(
                currentScreen = RegisterSubScreen.Start,
                userName = "",
                errorMessage = null,
                isLoading = false,
                onStartClicked = {},
                onNameChanged = {},
                onNameSubmitClicked = {},
                innerPadding = innerPadding
            )
        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun NewRegisterScreenSystemUIPreview() {
    LetterBLETheme {
        Scaffold { innerPadding ->
            RegisterScreenContent(
                currentScreen = RegisterSubScreen.NewRegistration,
                userName = "",
                errorMessage = null,
                isLoading = false,
                onStartClicked = {},
                onNameChanged = {},
                onNameSubmitClicked = {},
                innerPadding = innerPadding
            )
        }
    }
}

