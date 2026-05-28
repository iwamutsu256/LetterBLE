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
 * Repository boundary for BLE start/stop and encounter callbacks.
 *
 * Android BLE APIs stay in the BLE data sources. This class only connects the
 * detected userName to the relay use case.
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

        val started = bleController.start(myUserName) { foundUserName ->
            onEncounter(foundUserName)
        }
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

        notificationHelper.showEncounterNotification(targetUserName)

        scope.launch {
            try {
                relayLetterUseCase.execute(
                    myUserName = myUserName,
                    targetUserName = targetUserName
                )
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
