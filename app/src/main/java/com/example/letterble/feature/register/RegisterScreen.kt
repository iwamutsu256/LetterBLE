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

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.letterble.R
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
/*切り替わった画面を管理する*/
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
/*表示分岐画面二つに切り替える*/
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
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFFFFA))
    ) {

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
                .offset(x = (140).dp, y = (80).dp)
        )
        Image(
            painter = painterResource(id = R.drawable.img03),
            contentDescription = null,
            modifier = Modifier
                .size(350.dp)
                .offset(x = (100).dp, y = (650).dp)
        )
        Image(
            painter = painterResource(id = R.drawable.img04),
            contentDescription = null,
            modifier = Modifier
                .size(100.dp)
                .offset(x = (100).dp, y = (650).dp)
        )

        // Column（題名やボタン）
        Column(
            modifier = Modifier
                .fillMaxSize()
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
    onRegistered: () -> Unit,
    modifier: Modifier = Modifier
) {
    var name by rememberSaveable { mutableStateOf("") }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFF6242))
    ){
        Image(
            painter = painterResource(id = R.drawable.img06),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(850.dp)
                .offset(x = (5).dp, y = (-350).dp)
                .graphicsLayer(
                    rotationZ = -35f
                )
        )
        Text(
            text = "What's Your",
            fontSize = 50.sp,
            color = Color(0xFF0F0F6D),
            modifier = Modifier.offset(x = 50.dp, y = 60.dp)
        )
        Text(
            text = "Name?",
            fontSize = 50.sp,
            color = Color(0xFF0F0F6D),
            modifier = Modifier.offset(x = 190.dp, y = 120.dp)
        )
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.icon),
                contentDescription = null,
                modifier = Modifier
                    .size(250.dp)
            )
            // 入力欄
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = { Text("名前を入力") },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color(0xFF0F0F6D), //カーソル時の枠線の色
                    unfocusedIndicatorColor = Color(0xFF0F0F6D), //カーソルされてないときの線の色
                    cursorColor = Color(0xFF04041F) //入力時の|の色
                ),
                modifier = Modifier
                    .width(265.dp)
                    .height(80.dp)
                    .padding(top = 24.dp)
            )
            Button(
                modifier = Modifier
                    .padding(top = 40.dp)
                    .width(265.dp)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0F0F6D),   // 背景色
                    contentColor = Color(0xFFFFF01D)    // 文字色
                ),
                onClick = onRegistered
            ) {
                Text(
                    text = "次へ",
                    fontSize = 20.sp
                )
            }
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
