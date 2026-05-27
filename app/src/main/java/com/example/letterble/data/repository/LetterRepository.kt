/**
 * 手紙データをアプリ側から扱いやすい形で提供する Repository。
 *
 * Firestore の細かい読み書きは LetterFirestoreDataSource に任せ、
 * ViewModel / UseCase からはこの Repository を通して手紙を保存・取得する。
 */
package com.example.letterble.data.repository

import com.example.letterble.data.datasource.firestore.LetterFirestoreDataSource
import com.example.letterble.domain.model.Letter
import com.example.letterble.domain.model.Location
import com.example.letterble.domain.usecase.RelayLetterRepository
import com.example.letterble.domain.usecase.SubmitLetterRepository

/**
 * LETTERS コレクションを扱う Repository。
 *
 * 今の段階ではビジネスロジックを持たず、DataSource の薄いラッパーにしておく。
 */
class LetterRepository(
    private val letterFirestoreDataSource: LetterFirestoreDataSource = LetterFirestoreDataSource()
) : RelayLetterRepository, SubmitLetterRepository {
    /**
     * 新しい手紙を Firestore に保存する。
     *
     * 投函処理側からは sendLetter() として呼び、実際の保存は DataSource に任せる。
     */
    suspend fun sendLetter(letter: Letter) {
        letterFirestoreDataSource.saveLetter(letter)
    }

    /**
     * 投函に必要な手紙本体・初期位置・差出人の運搬リスト更新をまとめて保存する。
     */
    override suspend fun submitLetter(
        letter: Letter,
        initialLocation: Location
    ) {
        letterFirestoreDataSource.submitLetter(letter, initialLocation)
    }

    /**
     * 指定した ID の手紙を 1 件取得する。
     *
     * 詳細画面や Relay 処理で、手紙本体を確認するときに使う。
     */
    suspend fun getLetter(letterId: String): Letter? {
        return letterFirestoreDataSource.getLetter(letterId)
    }

    /**
     * 自分宛てに到達済みの手紙一覧を取得する。
     *
     * to_user == userName かつ is_survival == false の手紙が対象。
     */
    suspend fun getReceivedLetters(userName: String): List<Letter> {
        return letterFirestoreDataSource.getReceivedLetters(userName)
    }

    /**
     * 指定ユーザーが運搬中の手紙一覧を取得する。
     *
     * 運搬画面側では getCarryingLetters() という名前で呼べるようにしておく。
     */
    suspend fun getCarryingLetters(userName: String): List<Letter> {
        return letterFirestoreDataSource.getCarriedLetters(userName)
    }

    /**
     * 指定ユーザーが運搬中の未到達手紙一覧を取得する。
     *
     * Relay 処理側では「相手が carried している手紙」として使う。
     */
    override suspend fun getCarriedLetters(userName: String): List<Letter> {
        return letterFirestoreDataSource.getCarriedLetters(userName)
    }

    /**
     * 手紙がまだ拡散中かどうかを更新する。
     *
     * 宛先に届いたときは false にして、以後の Relay 対象から外す。
     */
    override suspend fun updateSurvival(letterId: String, isSurvival: Boolean) {
        letterFirestoreDataSource.updateSurvival(letterId, isSurvival)
    }
}
