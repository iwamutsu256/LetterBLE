package com.example.letterble.navigation

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

    fun receivedDetail(letterId: String): String = "received/$letterId"

    fun carryDetail(letterId: String): String = "carry/$letterId"
}
