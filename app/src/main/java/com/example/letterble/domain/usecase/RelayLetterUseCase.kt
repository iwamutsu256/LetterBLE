/**
 * RelayLetterUseCase.kt
 *
 * 役割:
 * - BLE のすれ違いをきっかけに、相手が運搬中の手紙を自分へ relay する中核ロジック。
 *
 * この UseCase は UI を持たず、Repository を組み合わせて relay 処理だけを担当する。
 */
package com.example.letterble.domain.usecase

import com.example.letterble.data.repository.EncounterRepository
import com.example.letterble.data.repository.LetterRepository
import com.example.letterble.data.repository.LocationRepository
import com.example.letterble.data.repository.TreeRepository

class RelayLetterUseCase(
    private val encounterRepository: EncounterRepository,
    private val letterRepository: LetterRepository,
    private val locationRepository: LocationRepository,
    private val treeRepository: TreeRepository
) {
    /**
     * myUserName が targetUserName とすれ違ったときに呼ばれる入口。
     *
     * 後続のチェックリストで、重複チェック、手紙取得、運搬リスト追加、
     * 位置保存、tree 更新、宛先到達判定をこの中へ段階的に追加する。
     */
    suspend fun execute(myUserName: String, targetUserName: String) {
        if (myUserName.isBlank() || targetUserName.isBlank()) {
            return
        }

        if (myUserName == targetUserName) {
            return
        }

        // #85 以降で Repository を使った relay 処理を追加する。
    }
}
