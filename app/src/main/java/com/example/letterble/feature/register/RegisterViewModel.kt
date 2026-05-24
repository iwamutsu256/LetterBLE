/**
 * RegisterViewModel.kt
 *
 * 役割:
 * - ユーザー入力の状態管理
 * - 登録処理の制御
 * - BLE開始トリガー
 *
 * 呼び出す:
 * - UserRepository
 * - BleRepository
 */

// TODO: userName状態をStateとして保持する
// TODO: onNameChangedでstateを更新する
// TODO: onNameSubmitClickedでUserRepository.saveUser()を呼ぶ
// TODO: 権限処理結果を受け取る関数を実装する
// TODO: 権限許可後にBleRepository.startBle()を呼ぶ
// TODO: 完了後にホーム画面遷移イベントをemitする
