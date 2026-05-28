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

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.example.letterble.navigation.AppNavGraph
import com.example.letterble.ui.theme.LetterBLETheme

class MainActivity : ComponentActivity() {
    private val blePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        if (results.values.all { it }) {
            appContainer().bleRepository.startBle()
        }
    }

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
        requestBlePermissionsIfNeeded()
    }

    override fun onDestroy() {
        appContainer().bleRepository.stopBle()
        super.onDestroy()
    }

    private fun requestBlePermissionsIfNeeded() {
        val missingPermissions = requiredBlePermissions().filter { permission ->
            ContextCompat.checkSelfPermission(this, permission) !=
                PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isEmpty()) {
            appContainer().bleRepository.startBle()
        } else {
            blePermissionLauncher.launch(missingPermissions.toTypedArray())
        }
    }

    private fun requiredBlePermissions(): List<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            listOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        } else {
            listOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
    }

    private fun appContainer() = (application as LetterBleApplication).appContainer
    }
