
/**
 * CurrentLocationDataSource.kt
 *
 * 役割:
 * - 端末の現在地を取得する
 * - 位置情報権限がない場合は null を返して上位層で扱えるようにする
 */
package com.example.letterble.data.datasource.location

import android.annotation.SuppressLint
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * FusedLocationProviderClient を使って現在地を1回取得するDataSource。
 */
class CurrentLocationDataSource(
    context: Context
) {
    private val applicationContext = context.applicationContext
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(applicationContext)

    /**
     * 現在地を取得する。権限未許可や端末側の取得失敗は呼び出し側で扱いやすいよう null にする。
     */
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Location? {
        if (!hasLocationPermission()) {
            return null
        }

        return suspendCancellableCoroutine { continuation ->
            val cancellationTokenSource =
                com.google.android.gms.tasks.CancellationTokenSource()

            try {
                // 呼び出し直前に権限確認済みだが、設定変更などで取り消された場合も null として扱う。
                fusedLocationClient
                    .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellationTokenSource.token)
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
            } catch (_: SecurityException) {
                if (continuation.isActive) {
                    continuation.resume(null)
                }
            }

            continuation.invokeOnCancellation {
                cancellationTokenSource.cancel()
            }
        }
    }

    private fun hasLocationPermission(): Boolean {
        val fineLocationGranted = ContextCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseLocationGranted = ContextCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineLocationGranted || coarseLocationGranted
    }
}
