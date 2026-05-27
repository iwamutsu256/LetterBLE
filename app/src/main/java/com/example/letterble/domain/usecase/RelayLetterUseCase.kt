/**
 * RelayLetterUseCase.kt
 *
 * 役割:
 * - BLE のすれ違いをきっかけに、相手が運搬中の手紙を自分へ relay する中核ロジック。
 *
 * この UseCase は UI を持たず、Repository を組み合わせて relay 処理だけを担当する。
 */
package com.example.letterble.domain.usecase

import com.example.letterble.domain.model.Encounter
import com.example.letterble.domain.model.Letter
import com.example.letterble.domain.model.Location
import java.util.UUID

class RelayLetterUseCase(
    private val encounterRepository: RelayEncounterRepository,
    private val letterRepository: RelayLetterRepository,
    private val locationRepository: RelayLocationRepository,
    private val treeRepository: RelayTreeRepository,
    private val userRepository: RelayUserRepository,
    private val duplicateIntervalMillis: Long = DEFAULT_DUPLICATE_INTERVAL_MILLIS,
    private val currentTimeMillis: () -> Long = { System.currentTimeMillis() },
    private val currentCoordinates: () -> RelayCoordinates = { RelayCoordinates() }
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

        val coordinates = currentCoordinates()
        val relayLocations = relayTargetLetters.map { letter ->
            Location(
                locationId = UUID.randomUUID().toString(),
                letterId = letter.letterId,
                userName = myUserName,
                latitude = coordinates.latitude,
                longitude = coordinates.longitude,
                timestamp = now
            )
        }

        relayLocations.forEach { location ->
            locationRepository.saveLocation(location)
        }

        // zipを使うと2つのリストを同じ順番でペアにできる。そのペアをforEachで一つずつ処理する。
        relayTargetLetters.zip(relayLocations).forEach { (letter, location) ->
            treeRepository.addNode(
                letterId = letter.letterId,
                parentUser = targetUserName,
                newUser = myUserName,
                location = location
            )
        }

        relayTargetLetters
            .filter { letter -> letter.toUser == myUserName }
            .forEach { letter ->
                letterRepository.updateSurvival(
                    letterId = letter.letterId,
                    isSurvival = false
                )
            }

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

data class RelayCoordinates(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)

interface RelayEncounterRepository {
    suspend fun saveEncounter(encounter: Encounter)
    suspend fun getLastEncounter(userA: String, userB: String): Encounter?
}

interface RelayLetterRepository {
    suspend fun getCarriedLetters(userName: String): List<Letter>
    suspend fun updateSurvival(letterId: String, isSurvival: Boolean)
}

interface RelayLocationRepository {
    suspend fun saveLocation(location: Location)
}

interface RelayTreeRepository {
    suspend fun addNode(
        letterId: String,
        parentUser: String,
        newUser: String,
        location: Location
    )
}

interface RelayUserRepository {
    suspend fun addCarryingLetterIds(userName: String, letterIds: List<String>)
}
