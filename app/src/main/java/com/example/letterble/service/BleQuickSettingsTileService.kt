package com.example.letterble.service

import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.example.letterble.LetterBleApplication

class BleQuickSettingsTileService : TileService() {
    private val appContainer
        get() = (application as LetterBleApplication).appContainer

    override fun onStartListening() {
        super.onStartListening()
        updateTile()
    }

    override fun onClick() {
        super.onClick()

        val bleStatusRepository = appContainer.bleStatusRepository
        if (bleStatusRepository.isBleEnabled()) {
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
        tile.state = if (state.isBleEnabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.label = "Letter BLE"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            tile.subtitle = if (state.isBleEnabled) "通信 ON" else "通信 OFF"
        }
        tile.updateTile()
    }
}
