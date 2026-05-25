/**
 * Letter.kt
 *
 * 役割:
 * - 手紙のデータモデル
 */
package com.example.letterble.domain.model

data class Letter(
    val letterId: String = "",
    val toUser: String = "",
    val fromUser: String = "",
    val sentence: String = "",
    val isSurvival: Boolean = true,
    val tree: Tree = Tree()
)
