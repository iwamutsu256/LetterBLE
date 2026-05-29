package com.example.letterble.data.repository

import android.util.Log
import com.example.letterble.data.datasource.ble.BleController
import com.example.letterble.domain.usecase.RelayLetterUseCase
import com.example.letterble.notification.BleNotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * BLE の開始・停止と、すれ違い検知後の中継処理をつなぐリポジトリ。
 *
 * Android BLE API の直接操作はデータソース側に置き、このクラスでは検知した
 * ユーザー名を手紙中継の UseCase へ渡す。
 */
class BleRepository(
    private val bleController: BleController,
    private val userRepository: UserRepository,
    private val relayLetterUseCase: RelayLetterUseCase,
    private val notificationHelper: BleNotificationHelper,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
) {
    fun startBle(): Boolean {
        val myUserName = userRepository.getCurrentUserName()?.takeIf { it.isNotBlank() }
            ?: return false

        if (bleController.isRunning) {
            notificationHelper.showBleRunningNotification(myUserName)
            return true
        }

        val started = bleController.start(
            myUserName,
            onUserFound = { foundUserName ->
                onEncounter(foundUserName)
            },
            onStartFailure = { errorMessage ->
                Log.e(TAG, "BLE start failed: $errorMessage")
                notificationHelper.hideBleRunningNotification()
            }
        )
        if (started) {
            notificationHelper.showBleRunningNotification(myUserName)
        }
        return started
    }

    fun stopBle() {
        bleController.stop()
        notificationHelper.hideBleRunningNotification()
    }

    fun onEncounter(targetUserName: String) {
        val myUserName = userRepository.getCurrentUserName()?.takeIf { it.isNotBlank() }
            ?: return

        if (targetUserName.isBlank() || targetUserName == myUserName) {
            return
        }

        scope.launch {
            try {
                notificationHelper.showEncounterNotification(targetUserName)

                val relayed = relayLetterUseCase.execute(
                    myUserName = myUserName,
                    targetUserName = targetUserName
                )
                if (relayed) {
                    notificationHelper.showRelayedNotification(targetUserName)
                } else {
                    Log.d(
                        TAG,
                        "BLE encounter detected, but no letters were relayed: $myUserName -> $targetUserName"
                    )
                }
            } catch (exception: Exception) {
                Log.e(
                    TAG,
                    "Failed to relay letters after BLE encounter: $myUserName -> $targetUserName",
                    exception
                )
            }
        }
    }

    private companion object {
        private const val TAG = "BleRepository"
    }
}
