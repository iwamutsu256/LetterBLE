/**
 * User.kt
 *
 * 役割:
 * - ユーザー情報モデル
 */
package com.example.letterble.domain.model

data class User(
    val userName: String = "",
    val carryingLetterIds: List<String> = emptyList()
)
