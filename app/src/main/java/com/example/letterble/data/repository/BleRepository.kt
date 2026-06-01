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
    private var isPreparingCurrentUserId = false
    private var shouldRunBle = false

    fun startBle(onPreparationFailure: () -> Unit = {}): Boolean {
        val myUserName = userRepository.getCurrentUserName()?.takeIf { it.isNotBlank() }
            ?: return false
        shouldRunBle = true

        if (bleController.isRunning) {
            notificationHelper.showBleRunningNotification(myUserName)
            return true
        }

        val myUserId = userRepository.getCurrentUserId()?.takeIf { it.isNotBlank() }
        if (myUserId == null) {
            prepareCurrentUserIdAndStartBle(myUserName, onPreparationFailure)
            return true
        }

        val started = bleController.start(
            myUserId,
            onUserFound = { foundUserId ->
                onEncounter(foundUserId)
            },
            onStartFailure = { errorMessage ->
                Log.e(TAG, "BLE start failed: $errorMessage")
                notificationHelper.hideBleRunningNotification()
            }
        )
        if (started) {
            notificationHelper.showBleRunningNotification(myUserName)
        } else {
            shouldRunBle = false
        }
        return started
    }

    private fun prepareCurrentUserIdAndStartBle(
        myUserName: String,
        onPreparationFailure: () -> Unit
    ) {
        if (isPreparingCurrentUserId) {
            return
        }

        isPreparingCurrentUserId = true
        scope.launch {
            try {
                val registration = userRepository.registerUser(myUserName)
                userRepository.saveCurrentUserId(registration.userId)
                if (shouldRunBle) {
                    startBle(onPreparationFailure)
                }
            } catch (exception: Exception) {
                Log.e(TAG, "Failed to prepare BLE user id: $myUserName", exception)
                shouldRunBle = false
                notificationHelper.hideBleRunningNotification()
                onPreparationFailure()
            } finally {
                isPreparingCurrentUserId = false
            }
        }
    }

    fun stopBle() {
        shouldRunBle = false
        bleController.stop()
        notificationHelper.hideBleRunningNotification()
    }

    fun onEncounter(targetUserId: String) {
        val myUserName = userRepository.getCurrentUserName()?.takeIf { it.isNotBlank() }
            ?: return
        val myUserId = userRepository.getCurrentUserId()?.takeIf { it.isNotBlank() }
            ?: myUserName

        if (targetUserId.isBlank() || targetUserId == myUserId) {
            return
        }

        scope.launch {
            var resolvedTargetUserName = targetUserId
            try {
                resolvedTargetUserName = userRepository.getUserNameByUserId(targetUserId)
                    ?: targetUserId

                if (resolvedTargetUserName.isBlank() || resolvedTargetUserName == myUserName) {
                    return@launch
                }

                val relayed = relayLetterUseCase.execute(
                    myUserName = myUserName,
                    targetUserName = resolvedTargetUserName
                )
                if (relayed) {
                    notificationHelper.showEncounterNotification(resolvedTargetUserName)
                } else {
                    Log.d(
                        TAG,
                        "BLE encounter detected, but no letters were relayed: $myUserName -> $resolvedTargetUserName"
                    )
                }
            } catch (exception: Exception) {
                Log.e(
                    TAG,
                    "Failed to relay letters after BLE encounter: $myUserName -> $resolvedTargetUserName",
                    exception
                )
            }
        }
    }

    private companion object {
        private const val TAG = "BleRepository"
    }
}
