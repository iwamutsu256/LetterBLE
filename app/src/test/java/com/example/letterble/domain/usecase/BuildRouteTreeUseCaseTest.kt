package com.example.letterble.domain.usecase

import com.example.letterble.domain.model.Edge
import com.example.letterble.domain.model.Location
import com.example.letterble.domain.model.Node
import com.example.letterble.domain.model.Tree
import org.junit.Assert.assertEquals
import org.junit.Test

class BuildRouteTreeUseCaseTest {

    private val useCase = BuildRouteTreeUseCase()

    @Test
    fun `Locationの時系列から直線経路のTreeを生成する`() {
        val locations = listOf(
            location(id = "node-b", userName = "B", timestamp = 200L),
            location(id = "node-a", userName = "A", timestamp = 100L),
            location(id = "node-c", userName = "C", timestamp = 300L)
        )

        val tree = useCase.buildTree(locations)

        assertEquals(listOf("node-a", "node-b", "node-c"), tree.nodes.map { node -> node.id })
        assertEquals(
            listOf(
                Edge(fromNodeId = "node-a", toNodeId = "node-b"),
                Edge(fromNodeId = "node-b", toNodeId = "node-c")
            ),
            tree.edges
        )
    }

    @Test
    fun `保存済みTreeに分岐がある場合はLocationから作り直さない`() {
        val savedTree = Tree(
            nodes = listOf(
                node(id = "node-a", userName = "A"),
                node(id = "node-b", userName = "B"),
                node(id = "node-c", userName = "C")
            ),
            edges = listOf(
                Edge(fromNodeId = "node-a", toNodeId = "node-b"),
                Edge(fromNodeId = "node-a", toNodeId = "node-c")
            )
        )
        val locations = listOf(
            location(id = "node-a", userName = "A", timestamp = 100L),
            location(id = "node-b", userName = "B", timestamp = 200L),
            location(id = "node-c", userName = "C", timestamp = 300L)
        )

        val tree = useCase.buildTree(savedTree = savedTree, locations = locations)

        // Locationだけでは分岐の親子関係を復元できないため、保存済みTreeの分岐を正として守る。
        assertEquals(savedTree, tree)
    }

    @Test
    fun `保存済みTreeが空ならLocationからTreeを生成する`() {
        val locations = listOf(
            location(id = "node-a", userName = "A", timestamp = 100L),
            location(id = "node-b", userName = "B", timestamp = 200L)
        )

        val tree = useCase.buildTree(savedTree = Tree(), locations = locations)

        assertEquals(2, tree.nodes.size)
        assertEquals(listOf(Edge(fromNodeId = "node-a", toNodeId = "node-b")), tree.edges)
    }

    private fun location(
        id: String,
        userName: String,
        timestamp: Long
    ): Location {
        return Location(
            locationId = id,
            letterId = "letter-1",
            userName = userName,
            latitude = 35.0,
            longitude = 139.0,
            timestamp = timestamp
        )
    }

    private fun node(
        id: String,
        userName: String
    ): Node {
        return Node(
            id = id,
            userName = userName,
            latitude = 35.0,
            longitude = 139.0
        )
    }
}
