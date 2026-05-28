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
        if (hasBleRuntimePermissions(results)) {
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
        val missingPermissions = startupPermissions().filter { permission ->
            ContextCompat.checkSelfPermission(this, permission) !=
                PackageManager.PERMISSION_GRANTED
        }

        if (hasBleRuntimePermissions()) {
            appContainer().bleRepository.startBle()
        }

        if (missingPermissions.isNotEmpty()) {
            blePermissionLauncher.launch(missingPermissions.toTypedArray())
        }
    }

    private fun startupPermissions(): List<String> {
        return requiredBlePermissions() + requiredNotificationPermissions()
    }

    private fun requiredBlePermissions(): List<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            listOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            listOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
    }

    private fun requiredNotificationPermissions(): List<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            listOf(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            emptyList()
        }
    }

    private fun hasBleRuntimePermissions(
        permissionResults: Map<String, Boolean>? = null
    ): Boolean {
        val nonLocationPermissions = requiredBlePermissions().filterNot { permission ->
            permission == Manifest.permission.ACCESS_COARSE_LOCATION ||
                permission == Manifest.permission.ACCESS_FINE_LOCATION
        }
        val hasNonLocationPermissions = nonLocationPermissions.all { permission ->
            permissionResults?.get(permission)
                ?: (ContextCompat.checkSelfPermission(this, permission) ==
                    PackageManager.PERMISSION_GRANTED)
        }
        val hasLocationPermission = hasPermission(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            permissionResults
        ) || hasPermission(
            Manifest.permission.ACCESS_FINE_LOCATION,
            permissionResults
        )

        return hasNonLocationPermissions && hasLocationPermission
    }

    private fun hasPermission(
        permission: String,
        permissionResults: Map<String, Boolean>? = null
    ): Boolean {
        return permissionResults?.get(permission)
            ?: (ContextCompat.checkSelfPermission(this, permission) ==
                PackageManager.PERMISSION_GRANTED)
    }

    private fun appContainer() = (application as LetterBleApplication).appContainer
    }
