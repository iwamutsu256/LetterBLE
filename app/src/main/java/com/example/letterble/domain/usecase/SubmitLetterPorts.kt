/**
 * 投函処理で使う Repository の最小ポート。
 *
 * UseCase は具体的な Firestore 実装を知らず、このインターフェース越しに保存する。
 */
package com.example.letterble.domain.usecase

import com.example.letterble.domain.model.Letter
import com.example.letterble.domain.model.Location

/**
 * 手紙本体を保存するためのポート。
 */
interface SubmitLetterRepository {
    suspend fun sendLetter(letter: Letter)
}

/**
 * 投函位置を保存するためのポート。
 */
interface SubmitLocationRepository {
    suspend fun saveLocation(location: Location)
}
