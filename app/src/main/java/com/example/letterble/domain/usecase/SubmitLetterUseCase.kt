/**
 * 手紙の投函処理をまとめる UseCase。
 */
package com.example.letterble.domain.usecase

import com.example.letterble.domain.model.Letter
import com.example.letterble.domain.model.Location
import java.util.UUID

/**
 * 投函に必要な入力値。
 */
data class SubmitLetterCommand(
    val fromUser: String,
    val toUser: String,
    val sentence: String,
    val latitude: Double,
    val longitude: Double
)

/**
 * 投函後に画面へ返す最小情報。
 */
data class SubmitLetterResult(
    val letterId: String,
    val locationId: String
)

/**
 * 手紙本体、差出人 root tree、投函位置を保存する。
 */
class SubmitLetterUseCase(
    private val letterRepository: SubmitLetterRepository,
    private val locationRepository: SubmitLocationRepository,
    private val buildInitialLetterTreeUseCase: BuildInitialLetterTreeUseCase = BuildInitialLetterTreeUseCase(),
    private val currentTimeMillis: () -> Long = { System.currentTimeMillis() },
    private val letterIdFactory: () -> String = { UUID.randomUUID().toString() }
) {
    suspend fun execute(command: SubmitLetterCommand): SubmitLetterResult {
        val fromUser = command.fromUser.trim()
        val toUser = command.toUser.trim()
        val sentence = command.sentence.trim()

        require(fromUser.isNotBlank()) { "fromUser is required" }
        require(toUser.isNotBlank()) { "toUser is required" }
        require(sentence.isNotBlank()) { "sentence is required" }

        val timestamp = currentTimeMillis()
        val letterId = letterIdFactory()
        val locationId = "${letterId}_posted_$timestamp"

        // 投函時点の座標を差出人 root node と位置履歴の両方に使う。
        val initialTree = buildInitialLetterTreeUseCase(
            fromUser = fromUser,
            latitude = command.latitude,
            longitude = command.longitude
        )

        val letter = Letter(
            letterId = letterId,
            toUser = toUser,
            fromUser = fromUser,
            sentence = sentence,
            isSurvival = true,
            tree = initialTree
        )
        val location = Location(
            locationId = locationId,
            letterId = letterId,
            userName = fromUser,
            latitude = command.latitude,
            longitude = command.longitude,
            timestamp = timestamp
        )

        letterRepository.sendLetter(letter)
        locationRepository.saveLocation(location)

        return SubmitLetterResult(
            letterId = letterId,
            locationId = locationId
        )
    }
}
