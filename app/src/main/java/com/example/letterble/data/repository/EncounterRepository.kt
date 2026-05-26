/**
 * すれ違い履歴をアプリ側から扱いやすい形で提供する Repository。
 *
 * Firestore の ENCOUNTERS コレクションへの保存・取得は
 * EncounterFirestoreDataSource に任せる。
 */
package com.example.letterble.data.repository

import com.example.letterble.data.datasource.firestore.EncounterFirestoreDataSource
import com.example.letterble.domain.model.Encounter

/**
 * ENCOUNTERS コレクションを扱う Repository。
 *
 * RelayLetterUseCase から、重複すれ違いチェックと履歴保存に使われる。
 */
class EncounterRepository(
    private val encounterFirestoreDataSource: EncounterFirestoreDataSource =
        EncounterFirestoreDataSource()
) {
    /**
     * すれ違い記録を Firestore に保存する。
     */
    suspend fun saveEncounter(encounter: Encounter) {
        encounterFirestoreDataSource.saveEncounter(encounter)
    }

    /**
     * 指定した2人の直近すれ違い記録を取得する。
     *
     * 一定時間内の重複 Relay を避ける判断材料になる。
     */
    suspend fun getLastEncounter(userA: String, userB: String): Encounter? {
        return encounterFirestoreDataSource.getLastEncounter(userA, userB)
    }
}
