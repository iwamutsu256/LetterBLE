/**
 * EditLetterViewModel.kt
 *
 * 役割:
 * - 手紙作成状態管理
 * - 下書き保存
 * - ポスト取得
 * - 手紙送信
 *
 * 呼び出す:
 * - DraftRepository
 * - PostRepository
 * - LetterRepository
 * - LocationRepository
 */

// TODO: UiState（toName, sentence, selectedPostなど）を定義する
// TODO: onToChangedでstate更新
// TODO: onSentenceChangedでstate更新
// TODO: onSaveDraftClickedでDraftRepository.saveDraft()を呼ぶ
// TODO: onSelectPostClickedでPostRepository.getNearbyPosts()を呼ぶ
// TODO: onPostSelectedでstate更新
// TODO: onSubmitClickedでLetterRepository.saveLetter()を呼ぶ
// TODO: 同時にLocationRepository.saveLocation()も呼ぶ
