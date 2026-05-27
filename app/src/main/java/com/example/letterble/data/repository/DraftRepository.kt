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

import com.example.letterble.data.datasource.local.DraftLocalDataSource

class DraftRepository(
    private val draftLocalDataSource: DraftLocalDataSource
) {

    // 宛先と本文をまとめて保存する。
    fun saveDraft(toName: String, sentence: String) =
        draftLocalDataSource.saveDraft(toName, sentence)

    // 保存されている宛先を取得する。
    fun loadDraftToName(): String = draftLocalDataSource.loadDraftToName()

    // 保存されている本文を取得する。
    fun loadDraftSentence(): String = draftLocalDataSource.loadDraftSentence()

    // 下書きを削除する。投函完了後に ViewModel から呼ばれる。
    fun clearDraft() = draftLocalDataSource.clearDraft()
}

// TODO: 各DataSourceを受け取る
// TODO: DataSourceの関数を呼び出すラッパーを作る
// TODO: 上位層に返すデータの整形を行う（必要なら）
// TODO: ビジネスロジックを書かない
