package com.example.letterble.data.repository

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * BLE 通信をユーザーが有効にしたいかどうかを端末内に保存する Repository。
 */
class BleStatusRepository(context: Context) {
    private val sharedPreferences = context.applicationContext.getSharedPreferences(
        BLE_STATUS_PREFS_NAME,
        Context.MODE_PRIVATE
    )

    private val _isBleEnabled = MutableStateFlow(isBleEnabled())
    val isBleEnabledState: StateFlow<Boolean> = _isBleEnabled.asStateFlow()

    fun isBleEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_BLE_ENABLED, true)
    }

    fun setBleEnabled(enabled: Boolean) {
        sharedPreferences.edit()
            .putBoolean(KEY_IS_BLE_ENABLED, enabled)
            .apply()
        _isBleEnabled.value = enabled
    }

    private companion object {
        private const val BLE_STATUS_PREFS_NAME = "ble_status"
        private const val KEY_IS_BLE_ENABLED = "is_ble_enabled"
    }
}
