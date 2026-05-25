/**
 * Tree.kt
 *
 * 役割:
 * - 表示用ツリー構造
 */
package com.example.letterble.domain.model

data class Tree(
    val nodes: List<Node>,
    val edges: List<Edge>
)
