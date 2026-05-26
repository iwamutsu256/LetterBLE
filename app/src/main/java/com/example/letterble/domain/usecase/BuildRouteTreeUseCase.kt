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

    fun buildTree(savedTree: Tree, locations: List<Location>): Tree {
        return if (savedTree.hasRoute()) {
            // Firestore の LETTERS.tree は中継時に親子関係つきで更新されるため、表示用の正とする。
            savedTree
        } else {
            buildTree(locations)
        }
    }

    fun buildTree(locations: List<Location>): Tree {
        if (locations.isEmpty()) {
            return Tree()
        }

        val sortedLocations = locations.sortedBy { location -> location.timestamp }

        val nodes = sortedLocations.mapIndexed { index, location ->
            location.toNode(index)
        }

        return Tree(
            nodes = nodes,
            // Location だけでは分岐時の親子関係が分からないため、推測で Edge は作らない。
            edges = emptyList()
        )
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

    private fun Tree.hasRoute(): Boolean {
        return nodes.isNotEmpty() || edges.isNotEmpty()
    }
}
