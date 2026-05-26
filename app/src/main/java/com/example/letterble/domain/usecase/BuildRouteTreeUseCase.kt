/**
 * BuildRouteTreeUseCase.kt
 *
 * 役割:
 * - locationsからtree構造を生成
 *
 * 注意:
 * - UI用データを作る専用
 */
package com.example.letterble.domain.usecase

import com.example.letterble.domain.model.Location
import com.example.letterble.domain.model.Node
import com.example.letterble.domain.model.Tree

class BuildRouteTreeUseCase {

    fun buildTree(locations: List<Location>): Tree {
        if (locations.isEmpty()) {
            return Tree()
        }

        val nodes = locations.mapIndexed { index, location ->
            location.toNode(index)
        }

        return Tree(nodes = nodes)
    }

    private fun Location.toNode(index: Int): Node {
        return Node(
            // locationId が空のテストデータでも、表示側で参照できる安定した id を作る。
            id = locationId.ifBlank { "location-$index" },
            userName = userName,
            latitude = latitude,
            longitude = longitude
        )
    }
}
