/**
 * DraftLocalDataSource.kt
 *
 * 役割:
 * - 端末内データ保存（下書き等）
 */

package com.example.letterble.data.datasource.local

import android.content.Context

class DraftLocalDataSource(context: Context) {

    private val sharedPreferences = context.getSharedPreferences(
        "letter_ble_draft_preferences",
        Context.MODE_PRIVATE
    )

    // 宛先と本文をまとめて保存する。
    fun saveDraft(toName: String, sentence: String) {
        sharedPreferences.edit()
            .putString("draft_to_name", toName)
            .putString("draft_sentence", sentence)
            .apply()
    }

    // 保存されている宛先を読み込む。なければ空文字を返す。
    fun loadDraftToName(): String {
        return sharedPreferences.getString("draft_to_name", "") ?: ""
    }

    // 保存されている本文を読み込む。なければ空文字を返す。
    fun loadDraftSentence(): String {
        return sharedPreferences.getString("draft_sentence", "") ?: ""
    }

    // 宛先と本文を両方削除する。投函完了後に呼ぶ。
    fun clearDraft() {
        sharedPreferences.edit()
            .remove("draft_to_name")
            .remove("draft_sentence")
            .apply()
    }
}

// TODO: SharedPreferencesのインスタンスを取得する
// TODO: saveDraftで文字列保存
// TODO: loadDraftで文字列読み込み
// TODO: clearDraftで削除
