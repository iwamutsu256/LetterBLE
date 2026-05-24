/**
 * RelayLetterUseCase.kt
 *
 * 役割:
 * - すれ違い処理の中心ロジック
 *
 * 処理内容:
 * - 重複チェック
 * - 手紙取得
 * - 手紙コピー
 * - location保存
 * - tree更新
 * - 宛先判定
 *
 * 呼び出す:
 * - LetterRepository
 * - LocationRepository
 * - EncounterRepository
 * - TreeRepository
 */

// TODO: execute(myUser, targetUser)関数を実装
// TODO: EncounterRepository.getLastEncounter()で重複チェック
// TODO: 条件外なら処理を終了
// TODO: EncounterRepository.saveEncounter()を呼ぶ
// TODO: LetterRepository.getCarriedLetters(targetUser)を呼ぶ
// TODO: is_survival == trueのみ対象にする
// TODO: 現在位置を取得（GPS）
// TODO: 各letterに対して以下を行う:
//      - copyLetter()
//      - saveLocation()
//      - addNode()
//      - 宛先判定してupdateSurvival()
//

// TODO: 非同期処理（coroutine）で実装する
