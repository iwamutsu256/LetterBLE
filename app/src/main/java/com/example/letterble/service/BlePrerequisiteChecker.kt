package com.example.letterble.service

import android.Manifest
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import androidx.core.content.ContextCompat

data class BlePrerequisiteReport(
    val isBluetoothEnabled: Boolean,
    val isLocationEnabled: Boolean,
    val hasBleRuntimePermissions: Boolean
) {
    val isReady: Boolean
        get() = isBluetoothEnabled && isLocationEnabled && hasBleRuntimePermissions
}

object BlePrerequisiteChecker {
    fun check(context: Context): BlePrerequisiteReport {
        return BlePrerequisiteReport(
            isBluetoothEnabled = isBluetoothEnabled(context),
            isLocationEnabled = isLocationEnabled(context),
            hasBleRuntimePermissions = hasBleRuntimePermissions(context)
        )
    }

    fun requiredBlePermissions(): List<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            listOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            listOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
    }

    private fun isBluetoothEnabled(context: Context): Boolean {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }

        return try {
            context.getSystemService(BluetoothManager::class.java)
                ?.adapter
                ?.isEnabled == true
        } catch (exception: SecurityException) {
            true
        }
    }

    private fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(LocationManager::class.java) ?: return false
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            locationManager.isLocationEnabled
        } else {
            @Suppress("DEPRECATION")
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        }
    }

    private fun hasBleRuntimePermissions(context: Context): Boolean {
        val bluetoothPermissions = requiredBlePermissions().filterNot { permission ->
            permission == Manifest.permission.ACCESS_COARSE_LOCATION ||
                permission == Manifest.permission.ACCESS_FINE_LOCATION
        }
        val hasBluetoothPermissions = bluetoothPermissions.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
        val hasLocationPermission =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

        return hasBluetoothPermissions && hasLocationPermission
    }
}
