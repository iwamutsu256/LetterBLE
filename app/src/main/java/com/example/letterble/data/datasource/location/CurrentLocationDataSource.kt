package com.example.letterble.data.datasource.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.example.letterble.domain.usecase.RelayCoordinates
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Reads the device's current position for relay route records.
 */
class CurrentLocationDataSource(
    private val context: Context
) {
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    suspend fun getCurrentCoordinates(): RelayCoordinates {
        if (!hasLocationPermission()) {
            return RelayCoordinates(latitude = 0.0, longitude = 0.0)
        }

        val location = suspendCancellableCoroutine { continuation ->
            fusedLocationClient
                .getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                .addOnSuccessListener { location ->
                    if (continuation.isActive) {
                        continuation.resume(location)
                    }
                }
                .addOnFailureListener {
                    if (continuation.isActive) {
                        continuation.resume(null)
                    }
                }
                .addOnCanceledListener {
                    if (continuation.isActive) {
                        continuation.resume(null)
                    }
                }
        }

        return RelayCoordinates(
            latitude = location?.latitude ?: 0.0,
            longitude = location?.longitude ?: 0.0
        )
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
    }
}
