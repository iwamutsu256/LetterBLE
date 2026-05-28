package com.example.letterble.data.datasource.ble

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context

interface BleController {
    val isRunning: Boolean
    fun start(userName: String, onUserFound: (String) -> Unit): Boolean
    fun stop()
}

/**
 * Coordinates BLE scan and advertise lifecycles as one data-source-level unit.
 */
class BleManager(
    context: Context,
    bluetoothAdapter: BluetoothAdapter? = context
        .getSystemService(BluetoothManager::class.java)
        ?.adapter
) : BleController {
    private val advertiser = BleAdvertiser(context, bluetoothAdapter)
    private val scanner = BleScanner(context, bluetoothAdapter)

    override val isRunning: Boolean
        get() = advertiser.isAdvertising || scanner.isScanning

    override fun start(userName: String, onUserFound: (String) -> Unit): Boolean {
        if (userName.isBlank()) {
            return false
        }

        val advertisingStarted = advertiser.startAdvertising(userName)
        val scanningStarted = scanner.startScanning(onUserFound)
        return advertisingStarted || scanningStarted
    }

    override fun stop() {
        scanner.stopScanning()
        advertiser.stopAdvertising()
    }
}
