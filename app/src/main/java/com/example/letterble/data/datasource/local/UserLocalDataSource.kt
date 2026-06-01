/**
 * UserLocalDataSource.kt
 *
 * 端末内にユーザー関連の小さな情報を保存するファイル。
 * 今回は「現在使っているユーザー名」と BLE 通信用ユーザーIDを SharedPreferences に保存する。
 */

// このファイルがローカル保存用 DataSource の置き場所にあることを示す。
package com.example.letterble.data.datasource.local

// SharedPreferences を作るために Android の Context を使う。
import android.content.Context

/**
 * ユーザー情報のローカル保存を担当する DataSource。
 *
 * Firestore ではなく、この端末の中だけに保存する。
 */
class UserLocalDataSource(
    // SharedPreferences を作るために Context を受け取る。
    context: Context
) {
    // 現在ユーザー名を保存するための SharedPreferences を用意する。
    private val sharedPreferences = context.getSharedPreferences(
        // SharedPreferences のファイル名。
        PREFERENCES_NAME,
        // このアプリだけが読める保存モード。
        Context.MODE_PRIVATE
    )

    /**
     * 現在ユーザー名を端末内に保存する。
     *
     * これがあると、次回起動時に登録画面をスキップできる。
     */
    fun saveCurrentUserName(userName: String) {
        // SharedPreferences の編集を開始する。
        sharedPreferences
            // 保存内容を変更するための Editor を取得する。
            .edit()
            // current_user_name というキーに userName を保存する。
            .putString(KEY_CURRENT_USER_NAME, userName)
            // 非同期で保存を反映する。
            .apply()
    }

    /**
     * 現在ユーザーの BLE 通信用IDを端末内に保存する。
     */
    fun saveCurrentUserId(userId: String) {
        sharedPreferences
            .edit()
            .putString(KEY_CURRENT_USER_ID, userId)
            .apply()
    }

    /**
     * 端末内に保存されている現在ユーザー名を取得する。
     *
     * まだ登録していない場合は null を返す。
     */
    fun getCurrentUserName(): String? {
        // current_user_name というキーの文字列を取得する。なければ null を返す。
        return sharedPreferences.getString(KEY_CURRENT_USER_NAME, null)
    }

    /**
     * 端末内に保存されている現在ユーザーの BLE 通信用IDを取得する。
     */
    fun getCurrentUserId(): String? {
        return sharedPreferences.getString(KEY_CURRENT_USER_ID, null)
    }

    /**
     * 端末内に保存されている現在ユーザー名を削除する。
     *
     * ログアウトや登録やり直しを作るときに使う。
     */
    fun clearCurrentUserName() {
        // SharedPreferences の編集を開始する。
        sharedPreferences
            // 保存内容を変更するための Editor を取得する。
            .edit()
            // current_user_name というキーの値を削除する。
            .remove(KEY_CURRENT_USER_NAME)
            .remove(KEY_CURRENT_USER_ID)
            // 非同期で削除を反映する。
            .apply()
    }

    /**
     * このクラスの中だけで使う定数。
     */
    private companion object {
        // SharedPreferences のファイル名。
        const val PREFERENCES_NAME = "letter_ble_user_preferences"

        // 現在ユーザー名を保存するときのキー名。
        const val KEY_CURRENT_USER_NAME = "current_user_name"

        // 現在ユーザーの BLE 通信用IDを保存するときのキー名。
        const val KEY_CURRENT_USER_ID = "current_user_id"
    }
}
