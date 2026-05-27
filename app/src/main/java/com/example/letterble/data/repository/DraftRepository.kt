/**
 * DraftRepository.kt
 *
 * 役割:
 * - 下書き保存管理
 *
 * 使用Datasource:
 * - Local（SharedPreferences）
 */
package com.example.letterble.data.repository

import com.example.letterble.data.datasource.local.DraftLetter
import com.example.letterble.data.datasource.local.DraftLocalDataSource

/**
 * 下書き保存を上位層へ公開するRepository。
 *
 * DataSource呼び出しのラッパーに留め、入力判定などの画面都合はViewModelで扱う。
 */
class DraftRepository(
    private val draftLocalDataSource: DraftLocalDataSource
) {
    /**
     * 下書きを保存する。
     */
    fun saveDraft(draft: DraftLetter) {
        draftLocalDataSource.saveDraft(draft)
    }

    /**
     * 下書きを読み込む。
     */
    fun loadDraft(): DraftLetter {
        return draftLocalDataSource.loadDraft()
    }

    /**
     * 下書きを削除する。
     */
    fun clearDraft() {
        draftLocalDataSource.clearDraft()
    }
}
