/**
 * Node.kt
 *
 * 役割:
 * - tree構造の構成要素
 */
package com.example.letterble.domain.model

data class Node(
    val id: String = "",
    val userName: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)
