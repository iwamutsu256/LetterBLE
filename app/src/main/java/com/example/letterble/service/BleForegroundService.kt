package com.example.letterble.service

import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.location.LocationManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import com.example.letterble.LetterBleApplication
import com.example.letterble.data.repository.BleStatusRepository
import com.example.letterble.notification.BleNotificationHelper

/**
 * アプリ画面が閉じられても BLE のスキャンとアドバタイズを続ける Foreground Service。
 */
class BleForegroundService : Service() {
    private val appContainer by lazy {
        (application as LetterBleApplication).appContainer
    }
    private var isSystemStateReceiverRegistered = false

    private val systemStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            when (intent?.action) {
                BluetoothAdapter.ACTION_STATE_CHANGED,
                LocationManager.PROVIDERS_CHANGED_ACTION -> stopIfPrerequisitesWereLost()
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        registerSystemStateReceiver()
    }

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
        unregisterSystemStateReceiver()
        appContainer.bleRepository.stopBle(disableByUser = false)
        super.onDestroy()
    }

    private fun startBleInForeground() {
        if (!appContainer.bleStatusRepository.isBleEnabled()) {
            Log.w(TAG, "Cannot start BLE foreground service while BLE is disabled by user.")
            stopSelf()
            return
        }

        val userName = appContainer.userRepository.getCurrentUserName()?.takeIf { it.isNotBlank() }
        if (userName == null) {
            Log.w(TAG, "Cannot start BLE foreground service without registered user name.")
            stopSelf()
            return
        }

        if (!BlePrerequisiteChecker.check(this).isReady) {
            Log.w(TAG, "Cannot start BLE foreground service while Bluetooth or location is off.")
            appContainer.bleRepository.stopBle(disableByUser = false)
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

        val started = appContainer.bleRepository.startBle(
            onPreparationFailure = {
                Log.w(TAG, "BLE foreground service could not prepare current user id.")
                stopSelf()
            }
        )
        if (!started) {
            Log.w(TAG, "BLE foreground service started, but BLE could not be started.")
            stopSelf()
        }
    }

    private fun stopBleAndSelf() {
        appContainer.bleRepository.stopBle(disableByUser = true)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun stopIfPrerequisitesWereLost() {
        val isBleRunning = appContainer.bleStatusRepository.statusState.value.isBleRunning
        if (!isBleRunning || BlePrerequisiteChecker.check(this).isReady) {
            return
        }

        Log.w(TAG, "Stopping BLE because Bluetooth or location was turned off.")
        appContainer.bleRepository.stopBle(disableByUser = false)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun registerSystemStateReceiver() {
        if (isSystemStateReceiverRegistered) {
            return
        }

        val filter = IntentFilter().apply {
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
            addAction(LocationManager.PROVIDERS_CHANGED_ACTION)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(systemStateReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("DEPRECATION")
            registerReceiver(systemStateReceiver, filter)
        }
        isSystemStateReceiverRegistered = true
    }

    private fun unregisterSystemStateReceiver() {
        if (!isSystemStateReceiverRegistered) {
            return
        }

        unregisterReceiver(systemStateReceiver)
        isSystemStateReceiverRegistered = false
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
            if (!BleStatusRepository(context).isBleEnabled()) {
                Log.w(TAG, "Skip starting BLE foreground service: BLE is disabled by user.")
                return
            }
            if (!hasRequiredPrerequisites(context)) {
                Log.w(TAG, "Skip starting BLE foreground service: required BLE prerequisite is missing.")
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

        private fun hasRequiredPrerequisites(context: Context): Boolean {
            return BlePrerequisiteChecker.check(context).isReady
        }
    }
}
