/**
 * 投函時に保存する初期 Tree を作る UseCase。
 */
package com.example.letterble.domain.usecase

import com.example.letterble.domain.model.Node
import com.example.letterble.domain.model.Tree

/**
 * 差出人を root node として持つ Tree を生成する。
 */
class BuildInitialLetterTreeUseCase {
    operator fun invoke(
        fromUser: String,
        latitude: Double,
        longitude: Double
    ): Tree {
        // 投函直後は中継がないため、edge は空で差出人 node だけを持つ。
        return Tree(
            nodes = listOf(
                Node(
                    id = fromUser,
                    userName = fromUser,
                    latitude = latitude,
                    longitude = longitude
                )
            ),
            edges = emptyList()
        )
    }
}
