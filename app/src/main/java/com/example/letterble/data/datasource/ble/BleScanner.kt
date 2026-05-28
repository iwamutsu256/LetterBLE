package com.example.letterble.data.datasource.ble

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import java.nio.charset.StandardCharsets

/**
 * 周囲の LetterBLE 広告をスキャンし、広告に含まれるユーザー名を取り出すデータソース。
 */
class BleScanner(
    private val context: Context,
    private val bluetoothAdapter: BluetoothAdapter?
) {
    private var scanCallback: ScanCallback? = null

    val isScanning: Boolean
        get() = scanCallback != null

    @SuppressLint("MissingPermission")
    fun startScanning(onUserFound: (String) -> Unit): Boolean {
        if (isScanning || !hasScanPermission()) {
            return false
        }

        val scanner = bluetoothAdapter
            ?.takeIf { it.isEnabled }
            ?.bluetoothLeScanner
            ?: return false

        val callback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                result.readUserName()?.let(onUserFound)
            }

            override fun onBatchScanResults(results: MutableList<ScanResult>) {
                results.mapNotNull { it.readUserName() }.forEach(onUserFound)
            }

            override fun onScanFailed(errorCode: Int) {
                if (scanCallback === this) {
                    scanCallback = null
                }
            }
        }

        val filter = ScanFilter.Builder()
            .setServiceUuid(BleAdvertiser.LETTER_BLE_SERVICE_UUID)
            .build()
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        scanCallback = callback
        scanner.startScan(listOf(filter), settings, callback)
        return true
    }

    @SuppressLint("MissingPermission")
    fun stopScanning() {
        val callback = scanCallback ?: return
        if (hasScanPermission()) {
            bluetoothAdapter?.bluetoothLeScanner?.stopScan(callback)
        }
        scanCallback = null
    }

    private fun ScanResult.readUserName(): String? {
        val payload = scanRecord
            ?.getServiceData(BleAdvertiser.LETTER_BLE_SERVICE_UUID)
            ?: return null
        return payload.toString(StandardCharsets.UTF_8).trim().takeIf { it.isNotBlank() }
    }

    private fun hasScanPermission(): Boolean {
        val hasMainPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
        }
        val hasConnectPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED

        return hasMainPermission && hasConnectPermission
    }
}
