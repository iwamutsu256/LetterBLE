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
import com.example.letterble.data.repository.UserRepository
import com.example.letterble.domain.model.Encounter
import java.util.UUID

class RelayLetterUseCase(
    private val encounterRepository: EncounterRepository,
    private val letterRepository: LetterRepository,
    private val locationRepository: LocationRepository,
    private val treeRepository: TreeRepository,
    private val userRepository: UserRepository,
    private val duplicateIntervalMillis: Long = DEFAULT_DUPLICATE_INTERVAL_MILLIS,
    private val currentTimeMillis: () -> Long = { System.currentTimeMillis() }
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

        val now = currentTimeMillis()
        if (isDuplicateEncounter(myUserName, targetUserName, now)) {
            return
        }

        encounterRepository.saveEncounter(
            Encounter(
                encounterId = UUID.randomUUID().toString(),
                userA = myUserName,
                userB = targetUserName,
                timestamp = now
            )
        )

        val targetCarriedLetters = letterRepository.getCarriedLetters(targetUserName)
        if (targetCarriedLetters.isEmpty()) {
            return
        }

        val relayTargetLetters = targetCarriedLetters.filter { letter ->
            letter.tree.nodes.none { node -> node.userName == myUserName }
        }
        if (relayTargetLetters.isEmpty()) {
            return
        }

        userRepository.addCarryingLetterIds(
            userName = myUserName,
            letterIds = relayTargetLetters.map { letter -> letter.letterId }
        )

        // #89 以降で位置情報保存から先の relay 処理を追加する。
    }

    private suspend fun isDuplicateEncounter(
        myUserName: String,
        targetUserName: String,
        now: Long
    ): Boolean {
        val lastEncounter = encounterRepository.getLastEncounter(myUserName, targetUserName)
            ?: return false

        return now - lastEncounter.timestamp < duplicateIntervalMillis
    }

    companion object {
        private const val DEFAULT_DUPLICATE_INTERVAL_MILLIS = 24L * 60L * 60L * 1000L
    }
}
