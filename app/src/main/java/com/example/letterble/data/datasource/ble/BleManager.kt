/**
 * BleManager.kt
 *
 * 役割:
 * - BLE全体制御
 * - スキャン＋アドバタイズ統合
 */

// TODO: BLEスキャン処理を実装する
// TODO: onDeviceFoundでcallbackを呼ぶ
// TODO: 自分のID（userName）をadvertiseする
// TODO: すれ違い発生時にcallbackを呼ぶ
//
// TODO: RelayLetterUseCase.execute()を呼ぶトリガーをここで発火する
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
 * BLE のスキャンとアドバタイズをまとめて開始・停止するデータソース。
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
        if (!advertisingStarted) {
            return false
        }

        val scanningStarted = scanner.startScanning(onUserFound)
        if (!scanningStarted) {
            advertiser.stopAdvertising()
            return false
        }

        return true
    }

    override fun stop() {
        scanner.stopScanning()
        advertiser.stopAdvertising()
    }
}
