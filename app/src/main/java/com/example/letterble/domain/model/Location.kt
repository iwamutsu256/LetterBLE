/**
 * Location.kt
 *
 * 役割:
 * - 手紙の中継履歴モデル
 */
package com.example.letterble.domain.model

data class Location(
    val locationId: String = "",
    val letterId: String = "",
    val userName: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val timestamp: Long = 0L
)
