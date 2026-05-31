package com.example.letterble.service

import android.Manifest
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.letterble.LetterBleApplication
import com.example.letterble.notification.BleNotificationHelper

/**
 * アプリ画面が閉じられても BLE のスキャンとアドバタイズを続ける Foreground Service。
 */
class BleForegroundService : Service() {
    private val appContainer by lazy {
        (application as LetterBleApplication).appContainer
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopBleAndSelf()
                return START_NOT_STICKY
            }
            else -> startBleInForeground()
        }

        return START_STICKY
    }

    override fun onDestroy() {
        appContainer.bleRepository.stopBle()
        super.onDestroy()
    }

    private fun startBleInForeground() {
        val userName = appContainer.userRepository.getCurrentUserName()?.takeIf { it.isNotBlank() }
        if (userName == null) {
            Log.w(TAG, "Cannot start BLE foreground service without registered user name.")
            stopSelf()
            return
        }

        val notification = BleNotificationHelper(this).createBleRunningNotification(userName)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    BleNotificationHelper.BLE_RUNNING_NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
                )
            } else {
                startForeground(BleNotificationHelper.BLE_RUNNING_NOTIFICATION_ID, notification)
            }
        } catch (exception: SecurityException) {
            Log.e(TAG, "Missing foreground service or Bluetooth permission.", exception)
            stopSelf()
            return
        }

        if (!appContainer.bleRepository.startBle()) {
            Log.w(TAG, "BLE foreground service started, but BLE could not be started.")
            stopSelf()
        }
    }

    private fun stopBleAndSelf() {
        appContainer.bleRepository.stopBle()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    companion object {
        private const val ACTION_START = "com.example.letterble.service.action.START_BLE"
        private const val ACTION_STOP = "com.example.letterble.service.action.STOP_BLE"
        private const val TAG = "BleForegroundService"

        fun startIfReady(context: Context, userName: String?) {
            if (userName.isNullOrBlank()) {
                Log.w(TAG, "Skip starting BLE foreground service: user is not registered.")
                return
            }
            if (!hasRequiredRuntimePermissions(context)) {
                Log.w(TAG, "Skip starting BLE foreground service: required permission is missing.")
                return
            }
            start(context)
        }

        private fun start(context: Context) {
            val intent = Intent(context, BleForegroundService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, BleForegroundService::class.java))
        }

        private fun hasRequiredRuntimePermissions(context: Context): Boolean {
            return requiredBlePermissions().all { permission ->
                ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
            }
        }

        private fun requiredBlePermissions(): List<String> {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                listOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_ADVERTISE,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            } else {
                listOf(Manifest.permission.ACCESS_COARSE_LOCATION)
            }
        }
    }
}
