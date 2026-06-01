package com.example.letterble.notification

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.letterble.R
import kotlin.math.roundToInt

/**
 * BLE の状態とすれ違い検知をユーザーへ知らせる通知ヘルパー。
 */
class BleNotificationHelper(
    private val context: Context
) {
    private val notificationManager = NotificationManagerCompat.from(context)
    private val largeIcon: Bitmap by lazy { createLargeIcon() }

    init {
        createChannels()
    }

    fun createBleRunningNotification(userName: String): Notification {
        return NotificationCompat.Builder(context, BLE_STATUS_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_small)
            .setLargeIcon(largeIcon)
            .setContentTitle("BLE通信中")
            .setContentText("$userName として周囲のユーザーを探しています")
            .setContentIntent(openAppPendingIntent())
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setShowWhen(false)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
    }

    fun showBleRunningNotification(userName: String) {
        if (!canPostNotifications()) {
            return
        }

        notifyIfAllowed(
            notificationId = BLE_RUNNING_NOTIFICATION_ID,
            notification = createBleRunningNotification(userName)
        )
    }

    fun hideBleRunningNotification() {
        notificationManager.cancel(BLE_RUNNING_NOTIFICATION_ID)
    }

    fun showEncounterNotification(targetUserName: String) {
        if (!canPostNotifications()) {
            return
        }

        notifyIfAllowed(
            notificationId = targetUserName.notificationId(),
            notification = NotificationCompat.Builder(context, BLE_EVENT_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_small)
                .setLargeIcon(largeIcon)
                .setContentTitle("すれ違いました")
                .setContentText("$targetUserName さんを検知しました")
                .setContentIntent(openAppPendingIntent())
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()
        )
    }

    private fun notifyIfAllowed(notificationId: Int, notification: Notification) {
        if (!canPostNotifications()) {
            return
        }

        try {
            notificationManager.notify(notificationId, notification)
        } catch (exception: SecurityException) {
            Log.w(TAG, "Notification permission was revoked before notify().", exception)
        }
    }

    private fun createChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                BLE_STATUS_CHANNEL_ID,
                "BLE通信状態",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "BLE通信中であることを固定表示します"
            }
        )
        manager.createNotificationChannel(
            NotificationChannel(
                BLE_EVENT_CHANNEL_ID,
                "すれ違い通知",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "近くのユーザーを検知したときに通知します"
            }
        )
    }

    private fun canPostNotifications(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
    }

    private fun openAppPendingIntent(): PendingIntent? {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            ?: return null
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getActivity(context, OPEN_APP_REQUEST_CODE, intent, flags)
    }

    private fun createLargeIcon(): Bitmap {
        val source = BitmapFactory.decodeResource(
            context.resources,
            R.drawable.icon,
            BitmapFactory.Options().apply { inScaled = false }
        )
        val scale = NOTIFICATION_LARGE_ICON_SIZE_PX.toFloat() / maxOf(source.width, source.height)
        val width = (source.width * scale).roundToInt()
        val height = (source.height * scale).roundToInt()
        return Bitmap.createScaledBitmap(
            source,
            width,
            height,
            true
        ).also {
            if (it != source) {
                source.recycle()
            }
        }
    }

    private fun String.notificationId(): Int {
        return ENCOUNTER_NOTIFICATION_ID_BASE + hashCode().let { hash ->
            if (hash == Int.MIN_VALUE) 0 else kotlin.math.abs(hash)
        } % ENCOUNTER_NOTIFICATION_ID_RANGE
    }

    companion object {
        const val BLE_RUNNING_NOTIFICATION_ID = 1001

        private const val BLE_STATUS_CHANNEL_ID = "letter_ble_status_v2"
        private const val BLE_EVENT_CHANNEL_ID = "letter_ble_events"
        private const val ENCOUNTER_NOTIFICATION_ID_BASE = 2000
        private const val ENCOUNTER_NOTIFICATION_ID_RANGE = 100_000
        private const val OPEN_APP_REQUEST_CODE = 3001
        private const val NOTIFICATION_LARGE_ICON_SIZE_PX = 128
        private const val TAG = "BleNotificationHelper"
    }
}
