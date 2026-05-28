package com.example.letterble.data.datasource.ble

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.ParcelUuid
import androidx.core.content.ContextCompat
import java.nio.charset.StandardCharsets

/**
 * 自分のユーザー名を BLE のサービスデータとして周囲に発信するデータソース。
 *
 * BLE 広告に入るデータ量は小さいため、送れない長さのユーザー名は途中で切らず、
 * 開始失敗として扱う。途中で切ると Firestore のユーザー名と一致しなくなるため。
 */
class BleAdvertiser(
    private val context: Context,
    private val bluetoothAdapter: BluetoothAdapter?
) {
    private var advertiseCallback: AdvertiseCallback? = null

    val isAdvertising: Boolean
        get() = advertiseCallback != null

    @SuppressLint("MissingPermission")
    fun startAdvertising(userName: String): Boolean {
        if (isAdvertising || userName.isBlank() || !hasAdvertisePermission()) {
            return false
        }

        val advertiser = bluetoothAdapter
            ?.takeIf { it.isEnabled }
            ?.bluetoothLeAdvertiser
            ?: return false

        val callback = object : AdvertiseCallback() {
            override fun onStartFailure(errorCode: Int) {
                if (advertiseCallback === this) {
                    advertiseCallback = null
                }
            }
        }

        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
            .setConnectable(false)
            .build()

        val payload = userName.toBlePayloadOrNull() ?: return false

        val data = AdvertiseData.Builder()
            .setIncludeDeviceName(false)
            .addServiceUuid(LETTER_BLE_SERVICE_UUID)
            .addServiceData(LETTER_BLE_SERVICE_UUID, payload)
            .build()

        advertiseCallback = callback
        advertiser.startAdvertising(settings, data, callback)
        return true
    }

    @SuppressLint("MissingPermission")
    fun stopAdvertising() {
        val callback = advertiseCallback ?: return
        if (hasAdvertisePermission()) {
            bluetoothAdapter?.bluetoothLeAdvertiser?.stopAdvertising(callback)
        }
        advertiseCallback = null
    }

    private fun hasAdvertisePermission(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_ADVERTISE
            ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
    }

    private fun String.toBlePayloadOrNull(): ByteArray? {
        val payload = toByteArray(StandardCharsets.UTF_8)
        return payload.takeIf { it.size <= MAX_USER_NAME_BYTES }
    }

    companion object {
        private const val MAX_USER_NAME_BYTES = 16
        val LETTER_BLE_SERVICE_UUID: ParcelUuid =
            ParcelUuid.fromString("0000feed-0000-1000-8000-00805f9b34fb")
    }
}
