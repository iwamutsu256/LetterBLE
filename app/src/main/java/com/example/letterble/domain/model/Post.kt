/**
 * Post.kt
 *
 * 役割:
 * - 郵便ポストの位置情報モデル
 */
package com.example.letterble.domain.model

data class Post(
    val id: String = "",
    val name: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val description: String = ""
)
