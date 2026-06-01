package com.example.letterble.data.repository

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class BleStatusState(
    val isBleEnabled: Boolean = true,
    val isBleRunning: Boolean = false,
    val hasShownTilePrompt: Boolean = false
)

/**
 * BLE 通信をユーザーが有効にしたいかどうかを端末内に保存する Repository。
 */
class BleStatusRepository(context: Context) {
    private val sharedPreferences = context.applicationContext.getSharedPreferences(
        BLE_STATUS_PREFS_NAME,
        Context.MODE_PRIVATE
    )

    private val _statusState = MutableStateFlow(loadStatusState())
    val statusState: StateFlow<BleStatusState> = _statusState.asStateFlow()

    fun isBleEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_BLE_ENABLED, true)
    }

    fun setBleEnabled(enabled: Boolean) {
        sharedPreferences.edit()
            .putBoolean(KEY_IS_BLE_ENABLED, enabled)
            .apply()
        _statusState.value = _statusState.value.copy(isBleEnabled = enabled)
    }

    fun setBleRunning(running: Boolean) {
        _statusState.value = _statusState.value.copy(isBleRunning = running)
    }

    fun markTilePromptShown() {
        sharedPreferences.edit()
            .putBoolean(KEY_HAS_SHOWN_TILE_PROMPT, true)
            .apply()
        _statusState.value = _statusState.value.copy(hasShownTilePrompt = true)
    }

    private fun loadStatusState(): BleStatusState {
        return BleStatusState(
            isBleEnabled = isBleEnabled(),
            hasShownTilePrompt = sharedPreferences.getBoolean(KEY_HAS_SHOWN_TILE_PROMPT, false)
        )
    }

    private companion object {
        private const val BLE_STATUS_PREFS_NAME = "ble_status"
        private const val KEY_IS_BLE_ENABLED = "is_ble_enabled"
        private const val KEY_HAS_SHOWN_TILE_PROMPT = "has_shown_tile_prompt"
    }
}
