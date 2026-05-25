/**
 * Post.kt
 *
 * 役割:
 * - 郵便ポストの位置情報モデル
 */
package com.example.letterble.domain.model

data class Post(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double
)
