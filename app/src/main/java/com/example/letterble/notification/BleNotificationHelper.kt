package com.example.letterble.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.letterble.R

/**
 * BLE の状態をユーザーに知らせる通知をまとめて扱うヘルパー。
 *
 * BLE の開始・停止やすれ違い処理そのものはリポジトリ / データソース側で行い、
 * このクラスでは通知チャンネルの作成と通知表示だけを担当する。
 */
class BleNotificationHelper(
    private val context: Context
) {
    private val notificationManager = NotificationManagerCompat.from(context)

    init {
        // Android 8.0 以降では通知を出す前にチャンネルを作っておく必要がある。
        createChannels()
    }

    /**
     * BLE 通信中であることを固定通知として表示する。
     *
     * setOngoing(true) にして、ユーザーが通信中であることを通知欄で確認できるようにする。
     */
    fun showBleRunningNotification(userName: String) {
        if (!canPostNotifications()) {
            return
        }

        notificationManager.notify(
            BLE_RUNNING_NOTIFICATION_ID,
            NotificationCompat.Builder(context, BLE_STATUS_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("BLE通信中")
                .setContentText("$userName として周囲のユーザーを探しています")
                .setContentIntent(openAppPendingIntent())
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setCategory(NotificationCompat.CATEGORY_STATUS)
                .setShowWhen(false)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()
        )
    }

    /**
     * BLE 通信中の固定通知を消す。
     */
    fun hideBleRunningNotification() {
        notificationManager.cancel(BLE_RUNNING_NOTIFICATION_ID)
    }

    /**
     * 周囲のユーザーを検知したときに、すれ違い通知を表示する。
     */
    fun showEncounterNotification(targetUserName: String) {
        if (!canPostNotifications()) {
            return
        }

        notificationManager.notify(
            targetUserName.notificationId(),
            NotificationCompat.Builder(context, BLE_EVENT_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("すれ違いました")
                .setContentText("$targetUserName さんを検知しました")
                .setContentIntent(openAppPendingIntent())
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()
        )
    }

    /**
     * BLE 通信中通知とすれ違い通知、それぞれの通知チャンネルを作成する。
     */
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

    /**
     * Android 13 以降では通知権限が必要なので、通知を出せる状態か確認する。
     */
    private fun canPostNotifications(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * 通知をタップしたときにアプリを開くための PendingIntent を作る。
     */
    private fun openAppPendingIntent(): PendingIntent? {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            ?: return null
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getActivity(context, OPEN_APP_REQUEST_CODE, intent, flags)
    }

    /**
     * 相手ユーザー名ごとに通知 ID を分け、すれ違い通知が上書きされすぎないようにする。
     */
    private fun String.notificationId(): Int {
        return ENCOUNTER_NOTIFICATION_ID_BASE + hashCode().let { hash ->
            if (hash == Int.MIN_VALUE) 0 else kotlin.math.abs(hash)
        } % ENCOUNTER_NOTIFICATION_ID_RANGE
    }

    companion object {
        // v2 にしているのは、古い LOW 重要度の通知チャンネル設定が端末に残るのを避けるため。
        private const val BLE_STATUS_CHANNEL_ID = "letter_ble_status_v2"
        private const val BLE_EVENT_CHANNEL_ID = "letter_ble_events"
        private const val BLE_RUNNING_NOTIFICATION_ID = 1001
        private const val ENCOUNTER_NOTIFICATION_ID_BASE = 2000
        private const val ENCOUNTER_NOTIFICATION_ID_RANGE = 100_000
        private const val OPEN_APP_REQUEST_CODE = 3001
    }
}
