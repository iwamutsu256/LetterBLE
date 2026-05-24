/**
 * アプリのエントリーポイント
 *
 * 役割:
 * - NavControllerを生成
 * - AppNavGraphを呼び出して画面遷移を開始する
 * - グローバルなUIテーマ適用
 *
 * 注意:
 * - ビジネスロジックは書かない
 * - ViewModelは持たない（画面ごとに持つ）
 */


package com.example.letterble

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.letterble.ui.theme.LetterBLETheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // TODO: アプリ全体のThemeを適用する
            LetterBLETheme {
                // TODO: アプリ全体のNavControllerを生成する
                // TODO: rememberNavController()を使ってNavControllerを作成する
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // TODO: AppNavGraph(navController)を呼び出す
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

// TODO: BLEやRepositoryの初期化を書く場合はここではなくDIで行う

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    LetterBLETheme {
        Greeting("Android")
    }
}