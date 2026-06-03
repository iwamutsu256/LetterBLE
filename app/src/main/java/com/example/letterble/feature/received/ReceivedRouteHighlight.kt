package com.example.letterble.feature.received

import com.example.letterble.domain.model.Edge
import com.example.letterble.domain.model.Tree

internal data class RouteHighlight(
    val nodeIds: Set<String>,
    val edges: Set<Edge>
)

internal fun Tree.shortestDirectedRouteHighlight(
    fromUser: String,
    toUser: String
): RouteHighlight {
    val trimmedFromUser = fromUser.trim()
    val trimmedToUser = toUser.trim()

    if (trimmedFromUser.isEmpty() || trimmedToUser.isEmpty()) {
        return RouteHighlight(nodeIds = emptySet(), edges = emptySet())
    }

    val nodesById = nodes.associateBy { node -> node.id }
    val goalNodes = nodes.filter { node -> node.userName == trimmedToUser }
    if (goalNodes.isEmpty()) {
        return RouteHighlight(nodeIds = emptySet(), edges = emptySet())
    }

    val incomingEdgesByToNodeId = edges
        .filter { edge -> edge.fromNodeId in nodesById && edge.toNodeId in nodesById }
        .groupBy { edge -> edge.toNodeId }
    val queue = ArrayDeque<String>()
    val visitedNodeIds = mutableSetOf<String>()
    val edgeToNextByNodeId = mutableMapOf<String, Edge>()

    goalNodes.forEach { goalNode ->
        queue.addLast(goalNode.id)
        visitedNodeIds += goalNode.id
    }

    while (queue.isNotEmpty()) {
        val currentNodeId = queue.removeFirst()
        val currentNode = nodesById[currentNodeId] ?: continue
        if (currentNode.userName == trimmedFromUser) {
            return buildRouteHighlightFrom(
                sourceNodeId = currentNode.id,
                edgeToNextByNodeId = edgeToNextByNodeId
            )
        }

        incomingEdgesByToNodeId[currentNodeId].orEmpty().forEach { incomingEdge ->
            if (visitedNodeIds.add(incomingEdge.fromNodeId)) {
                edgeToNextByNodeId[incomingEdge.fromNodeId] = incomingEdge
                queue.addLast(incomingEdge.fromNodeId)
            }
        }
    }

    return RouteHighlight(nodeIds = emptySet(), edges = emptySet())
}

private fun buildRouteHighlightFrom(
    sourceNodeId: String,
    edgeToNextByNodeId: Map<String, Edge>
): RouteHighlight {
    val routeNodeIds = linkedSetOf(sourceNodeId)
    val routeEdges = linkedSetOf<Edge>()
    var currentNodeId = sourceNodeId

    while (true) {
        val edge = edgeToNextByNodeId[currentNodeId] ?: break
        routeEdges += edge
        routeNodeIds += edge.toNodeId
        currentNodeId = edge.toNodeId
    }

    return RouteHighlight(nodeIds = routeNodeIds, edges = routeEdges)
}
