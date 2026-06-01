package com.example.letterble.service

import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.example.letterble.LetterBleApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class BleQuickSettingsTileService : TileService() {
    private var listeningScope: CoroutineScope? = null

    private val appContainer
        get() = (application as LetterBleApplication).appContainer

    override fun onStartListening() {
        super.onStartListening()
        listeningScope?.cancel()
        listeningScope = CoroutineScope(Job() + Dispatchers.Main.immediate).also { scope ->
            scope.launch {
                appContainer.bleStatusRepository.statusState.collect {
                    updateTile()
                }
            }
        }
        updateTile()
    }

    override fun onStopListening() {
        listeningScope?.cancel()
        listeningScope = null
        super.onStopListening()
    }

    override fun onClick() {
        super.onClick()

        val bleStatusRepository = appContainer.bleStatusRepository
        val state = bleStatusRepository.statusState.value
        val isBleActive = state.isBleEnabled && state.isBleRunning
        if (isBleActive) {
            bleStatusRepository.setBleEnabled(false)
            BleForegroundService.stop(this)
        } else {
            bleStatusRepository.setBleEnabled(true)
            BleForegroundService.startIfReady(
                context = this,
                userName = appContainer.userRepository.getCurrentUserName()
            )
        }

        updateTile()
    }

    private fun updateTile() {
        val tile = qsTile ?: return
        val state = appContainer.bleStatusRepository.statusState.value
        val isBleActive = state.isBleEnabled && state.isBleRunning
        tile.state = if (isBleActive) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.label = "Letter BLE"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            tile.subtitle = if (isBleActive) "通信 ON" else "通信 OFF"
        }
        tile.updateTile()
    }
}
