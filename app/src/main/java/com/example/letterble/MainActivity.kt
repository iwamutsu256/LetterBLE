package com.example.letterble

import android.Manifest
import android.app.StatusBarManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.example.letterble.navigation.AppNavGraph
import com.example.letterble.service.BleForegroundService
import com.example.letterble.service.BlePrerequisiteChecker
import com.example.letterble.service.BlePrerequisiteReport
import com.example.letterble.service.BleQuickSettingsTileService
import com.example.letterble.ui.theme.LetterBLETheme

class MainActivity : ComponentActivity() {
    private var blePermissionErrorMessage by mutableStateOf<String?>(null)
    private var bleSetupReport by mutableStateOf<BlePrerequisiteReport?>(null)
    private var shouldStartBleAfterSetup by mutableStateOf(false)

    private val blePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        if (hasBleRuntimePermissions(results)) {
            blePermissionErrorMessage = null
            if (shouldStartBleAfterSetup) {
                refreshBleSetupState()
            } else {
                startBleServiceIfReady()
            }
        } else {
            blePermissionErrorMessage = BLE_PERMISSION_MESSAGE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LetterBLETheme {
                val navController = rememberNavController()
                val appContainer = (application as LetterBleApplication).appContainer
                AppNavGraph(
                    navController = navController,
                    appContainer = appContainer,
                    blePermissionErrorMessage = blePermissionErrorMessage,
                    bleSetupReport = bleSetupReport,
                    onOpenAppSettingsClicked = ::openAppSettings,
                    onOpenBluetoothSettingsClicked = ::openBluetoothSettings,
                    onOpenLocationSettingsClicked = ::openLocationSettings,
                    onRequestBlePermissionsClicked = ::requestBlePermissionsIfNeeded,
                    onDismissBleSetupClicked = ::dismissBleSetupPrompt,
                    onRequestAddBleTileClicked = ::requestAddBleTile
                )
            }
        }
        handleBleSetupIntent(intent)
        requestBlePermissionsIfNeeded()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleBleSetupIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        refreshBlePermissionState()
        if (shouldStartBleAfterSetup) {
            refreshBleSetupState()
        }
    }

    private fun requestBlePermissionsIfNeeded() {
        val missingPermissions = startupPermissions().filter { permission ->
            ContextCompat.checkSelfPermission(this, permission) !=
                PackageManager.PERMISSION_GRANTED
        }

        if (hasBleRuntimePermissions()) {
            blePermissionErrorMessage = null
            if (!shouldStartBleAfterSetup) {
                startBleServiceIfReady()
            }
        } else {
            blePermissionErrorMessage = BLE_PERMISSION_MESSAGE
        }

        if (missingPermissions.isNotEmpty()) {
            blePermissionLauncher.launch(missingPermissions.toTypedArray())
        }
    }

    private fun refreshBlePermissionState() {
        if (hasBleRuntimePermissions()) {
            blePermissionErrorMessage = null
            if (!shouldStartBleAfterSetup) {
                startBleServiceIfReady()
            }
        } else {
            blePermissionErrorMessage = BLE_PERMISSION_MESSAGE
        }
    }

    private fun handleBleSetupIntent(intent: Intent?) {
        if (intent?.action != ACTION_SHOW_BLE_SETUP) {
            return
        }
        shouldStartBleAfterSetup = true
        refreshBleSetupState()
    }

    private fun refreshBleSetupState() {
        val report = BlePrerequisiteChecker.check(this)
        if (report.isReady) {
            bleSetupReport = null
            blePermissionErrorMessage = null
            val appContainer = (application as LetterBleApplication).appContainer
            appContainer.bleStatusRepository.setBleEnabled(true)
            startBleServiceIfReady()
            shouldStartBleAfterSetup = false
        } else {
            bleSetupReport = report
        }
    }

    private fun dismissBleSetupPrompt() {
        bleSetupReport = null
        shouldStartBleAfterSetup = false
    }

    private fun openAppSettings() {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", packageName, null)
        )
        startActivity(intent)
    }

    private fun openBluetoothSettings() {
        startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
    }

    private fun openLocationSettings() {
        startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
    }

    private fun requestAddBleTile() {
        val appContainer = (application as LetterBleApplication).appContainer
        appContainer.bleStatusRepository.markTilePromptShown()

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return
        }

        val statusBarManager = getSystemService(StatusBarManager::class.java) ?: return
        statusBarManager.requestAddTileService(
            ComponentName(this, BleQuickSettingsTileService::class.java),
            getString(R.string.ble_tile_label),
            Icon.createWithResource(this, R.drawable.ic_notification_small),
            mainExecutor
        ) {
            // The result is advisory; avoid showing the prompt repeatedly either way.
        }
    }

    private fun startBleServiceIfReady() {
        val appContainer = (application as LetterBleApplication).appContainer
        BleForegroundService.startIfReady(
            context = this,
            userName = appContainer.userRepository.getCurrentUserName()
        )
    }

    private fun startupPermissions(): List<String> {
        return requiredBlePermissions() + requiredNotificationPermissions()
    }

    private fun requiredBlePermissions(): List<String> {
        return BlePrerequisiteChecker.requiredBlePermissions()
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

    companion object {
        private const val ACTION_SHOW_BLE_SETUP =
            "com.example.letterble.action.SHOW_BLE_SETUP"
        private const val BLE_PERMISSION_MESSAGE =
            "BLEを使うにはBluetoothと位置情報の権限が必要です"

        fun createBleSetupIntent(context: Context): Intent {
            return Intent(context, MainActivity::class.java).apply {
                action = ACTION_SHOW_BLE_SETUP
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
        }
    }
}
