/**
 * DraftLocalDataSource.kt
 *
 * 役割:
 * - 端末内データ保存（下書き等）
 */
package com.example.letterble.data.datasource.local

import android.content.Context
import android.content.SharedPreferences

/**
 * 1通だけ保持する手紙下書き。
 */
data class DraftLetter(
    val toUser: String = "",
    val sentence: String = ""
)

/**
 * SharedPreferencesで下書きを1件だけ保存するDataSource。
 */
class DraftLocalDataSource(context: Context) {
    private val preferences: SharedPreferences =
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    /**
     * 宛先と本文を端末内に保存する。
     */
    fun saveDraft(draft: DraftLetter) {
        preferences.edit()
            .putString(KEY_TO_USER, draft.toUser)
            .putString(KEY_SENTENCE, draft.sentence)
            .apply()
    }

    /**
     * 保存済み下書きを読み込む。未保存の場合は空の下書きを返す。
     */
    fun loadDraft(): DraftLetter {
        return DraftLetter(
            toUser = preferences.getString(KEY_TO_USER, "").orEmpty(),
            sentence = preferences.getString(KEY_SENTENCE, "").orEmpty()
        )
    }

    /**
     * 端末内の下書きを削除する。
     */
    fun clearDraft() {
        preferences.edit()
            .remove(KEY_TO_USER)
            .remove(KEY_SENTENCE)
            .apply()
    }

    private companion object {
        const val PREFERENCES_NAME = "draft_preferences"
        const val KEY_TO_USER = "draft_to_user"
        const val KEY_SENTENCE = "draft_sentence"
    }
}
