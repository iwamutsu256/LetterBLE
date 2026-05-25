/**
 * Destinations.kt
 *
 * 役割:
 * - 画面遷移のルート文字列を一元管理する
 * - 文字列の打ち間違いを防ぐ
 * - 引数付きルートの形式をまとめる
 */
package com.example.letterble.navigation

import android.net.Uri

/**
 * 画面遷移の route 文字列を一元管理する。
 */
object Destinations {
    const val REGISTER = "register"
    const val HOME = "home"
    const val EDIT_LETTER = "edit_letter"
    const val RECEIVED = "received"
    const val CARRY = "carry"

    const val LETTER_ID_ARG = "letterId"

    const val RECEIVED_DETAIL = "received/{$LETTER_ID_ARG}"
    const val CARRY_DETAIL = "carry/{$LETTER_ID_ARG}"

    /**
     * 受信手紙の詳細画面へ遷移するための実 route を作成する。
     *
     * @param letterId 詳細表示する手紙ID
     */
    fun receivedDetail(letterId: String): String = "received/${Uri.encode(letterId)}"

    /**
     * 運搬中手紙の詳細画面へ遷移するための実 route を作成する。
     *
     * @param letterId 詳細表示する手紙ID
     */
    fun carryDetail(letterId: String): String = "carry/${Uri.encode(letterId)}"
}
