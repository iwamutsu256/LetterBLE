/**
 * Location.kt
 *
 * 役割:
 * - 手紙の中継履歴モデル
 */
package com.example.letterble.domain.model

data class Location(
    val locationId: String,
    val letterId: String,
    val userName: String,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long
)
