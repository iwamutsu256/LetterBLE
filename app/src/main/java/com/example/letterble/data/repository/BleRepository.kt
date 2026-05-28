package com.example.letterble.data.repository

import com.example.letterble.data.datasource.ble.BleController
import com.example.letterble.domain.usecase.RelayLetterUseCase
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
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
) {
    fun startBle(): Boolean {
        val myUserName = userRepository.getCurrentUserName()?.takeIf { it.isNotBlank() }
            ?: return false

        return bleController.start(myUserName) { foundUserName ->
            onEncounter(foundUserName)
        }
    }

    fun stopBle() {
        bleController.stop()
    }

    fun onEncounter(targetUserName: String) {
        val myUserName = userRepository.getCurrentUserName()?.takeIf { it.isNotBlank() }
            ?: return

        if (targetUserName.isBlank() || targetUserName == myUserName) {
            return
        }

        scope.launch {
            relayLetterUseCase.execute(
                myUserName = myUserName,
                targetUserName = targetUserName
            )
        }
    }
}
