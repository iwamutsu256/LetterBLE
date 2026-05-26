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
import androidx.navigation.compose.rememberNavController
import com.example.letterble.navigation.AppNavGraph
import com.example.letterble.ui.theme.LetterBLETheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LetterBLETheme {
                val navController = rememberNavController()
                // Application に用意した AppContainer を画面遷移グラフへ渡す。
                val appContainer = (application as LetterBleApplication).appContainer
                AppNavGraph(
                    navController = navController,
                    appContainer = appContainer
                )
            }
        }
    }
}
