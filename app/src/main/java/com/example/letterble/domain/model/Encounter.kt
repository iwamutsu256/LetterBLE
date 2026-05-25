/**
 * Encounter.kt
 *
 * 役割:
 * - すれ違いイベントの記録モデル
 */
package com.example.letterble.domain.model

data class Encounter(
    val encounterId: String,
    val userA: String,
    val userB: String,
    val timestamp: Long
)
