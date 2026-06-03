package com.example.letterble.feature.received

import com.example.letterble.domain.model.Edge
import com.example.letterble.domain.model.Node
import com.example.letterble.domain.model.Tree
import org.junit.Assert.assertEquals
import org.junit.Test

class ReceivedRouteHighlightTest {
    @Test
    fun `highlights only directed route from sender to receiver`() {
        val tree = Tree(
            nodes = listOf(
                Node(id = "alice", userName = "Alice"),
                Node(id = "relay-1", userName = "Relay1"),
                Node(id = "relay-2", userName = "Relay2"),
                Node(id = "bob", userName = "Bob"),
                Node(id = "other", userName = "Other")
            ),
            edges = listOf(
                Edge(fromNodeId = "alice", toNodeId = "relay-1"),
                Edge(fromNodeId = "relay-1", toNodeId = "relay-2"),
                Edge(fromNodeId = "relay-2", toNodeId = "bob"),
                Edge(fromNodeId = "alice", toNodeId = "other")
            )
        )

        val highlight = tree.shortestDirectedRouteHighlight(fromUser = "Alice", toUser = "Bob")

        assertEquals(setOf("bob", "relay-2", "relay-1", "alice"), highlight.nodeIds)
        assertEquals(setOf("bob", "alice"), highlight.endpointNodeIds)
        assertEquals(
            setOf(
                Edge(fromNodeId = "relay-2", toNodeId = "bob"),
                Edge(fromNodeId = "relay-1", toNodeId = "relay-2"),
                Edge(fromNodeId = "alice", toNodeId = "relay-1")
            ),
            highlight.edges
        )
    }

    @Test
    fun `returns empty highlight when directed route does not reach sender`() {
        val tree = Tree(
            nodes = listOf(
                Node(id = "alice", userName = "Alice"),
                Node(id = "relay", userName = "Relay"),
                Node(id = "bob", userName = "Bob")
            ),
            edges = listOf(
                Edge(fromNodeId = "relay", toNodeId = "bob")
            )
        )

        val highlight = tree.shortestDirectedRouteHighlight(fromUser = "Alice", toUser = "Bob")

        assertEquals(emptySet<String>(), highlight.nodeIds)
        assertEquals(emptySet<Edge>(), highlight.edges)
        assertEquals(emptySet<String>(), highlight.endpointNodeIds)
    }

    @Test
    fun `uses shortest directed route when multiple receiver nodes exist`() {
        val tree = Tree(
            nodes = listOf(
                Node(id = "alice", userName = "Alice"),
                Node(id = "relay-1", userName = "Relay1"),
                Node(id = "relay-2", userName = "Relay2"),
                Node(id = "bob-long", userName = "Bob"),
                Node(id = "bob-short", userName = "Bob")
            ),
            edges = listOf(
                Edge(fromNodeId = "alice", toNodeId = "relay-1"),
                Edge(fromNodeId = "relay-1", toNodeId = "relay-2"),
                Edge(fromNodeId = "relay-2", toNodeId = "bob-long"),
                Edge(fromNodeId = "alice", toNodeId = "bob-short")
            )
        )

        val highlight = tree.shortestDirectedRouteHighlight(fromUser = "Alice", toUser = "Bob")

        assertEquals(setOf("alice", "bob-short"), highlight.nodeIds)
        assertEquals(setOf("alice", "bob-short"), highlight.endpointNodeIds)
        assertEquals(setOf(Edge(fromNodeId = "alice", toNodeId = "bob-short")), highlight.edges)
    }
}
