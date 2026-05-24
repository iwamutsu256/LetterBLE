# AGENT.md

## プロジェクト概要

BLEによる「すれ違い」をトリガーとして、手紙データをユーザー間で中継・拡散するAndroidアプリ。

主な機能:
- ユーザー登録
- 手紙作成・送信
- BLEすれ違いによる手紙中継
- 手紙の受信・運搬
- 経路ツリーの可視化

背景:
- ハッカソン向け（1週間半、5人チーム：デザイン2名・開発3名）
- iPhoneメンバーがいるためエミュレータでの動作確認を考慮する

---

## 担当
 
| 担当者 | 役割 | 担当Task |
|---|---|---|
| 柴山 | 基盤・データ層 | Task 2, 4, 6, 7 |
| 竹花 | UI・画面層 | Task 3, 5, 8, 9, 16 |
| 岩﨑 | ロジック・機能層 | Task 10, 11, 12, 13, 14, 15 |
| 清野・柴 | デザイン | Task 17, 18 + 画面デザイン |
 
---

## アーキテクチャ

クリーンアーキテクチャを採用

構造:

UI → ViewModel → UseCase → Repository → DataSource

---

## ディレクトリ構成

```
feature/       各画面（UI + ViewModel）
navigation/    ナビゲーション
domain/        UseCase + Model
data/          Repository + DataSource
ui/            共通UI
di/            AppContainer.kt（依存関係の生成
```

---

## 各層の責務

### UI

- 表示のみ
- ViewModelのstateを描画
- イベントをViewModelへ通知

禁止:
- Repository呼び出し
- ロジック実装

---

### ViewModel

- 状態管理（UiState）
- UIイベント処理
- UseCase / Repository呼び出し
- Navigationイベント発火

禁止:
- NavController使用
- DataSource直接呼び出し

---

### UseCase

- ビジネスロジック
- 複数Repositoryの統合
- 非同期処理

例:
- RelayLetterUseCase
- BuildRouteTreeUseCase

---

### Repository

- データ取得・保存
- DataSourceのラッパー

禁止:
- 複雑なロジック

---

### DataSource

- Firestore / BLE / local へのアクセス
- 外部依存の実装

---

## データモデル
 
### Firestoreコレクション名
 
```
USERS
LETTERS
LOCATIONS
ENCOUNTERS
```

### 各モデルのフィールド
 
**User**
```kotlin
data class User(
    val userName: String = "",           // PK・ユーザー名（本名推奨）
    val carryingLetterIds: List<String> = emptyList()  // 運搬中の手紙IDリスト
)
```
 
**Letter**
```kotlin
data class Letter(
    val letterId: String = "",
    val toUser: String = "",             // 宛先ユーザー名
    val fromUser: String = "",           // 差出人ユーザー名
    val sentence: String = "",           // 本文
    val isSurvival: Boolean = true,      // 未到達=true、到達済み=false
    val tree: Tree = Tree()              // 木構造（nodes / edges）
)
```
 
**Location**
```kotlin
data class Location(
    val locationId: String = "",
    val letterId: String = "",
    val userName: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val timestamp: Long = 0L
)
```
 
**Encounter**
```kotlin
data class Encounter(
    val encounterId: String = "",
    val userA: String = "",
    val userB: String = "",
    val timestamp: Long = 0L
)
```
 
**Node**
```kotlin
data class Node(
    val id: String = "",
    val userName: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)
```
 
**Edge**
```kotlin
data class Edge(
    val fromNodeId: String = "",
    val toNodeId: String = ""
)
```
 
**Tree**
```kotlin
data class Tree(
    val nodes: List<Node> = emptyList(),
    val edges: List<Edge> = emptyList()
)
```
 
**Post**
```kotlin
data class Post(
    val id: String = "",
    val name: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)
```

### Firestoreフィールド名の対応
 
Kotlinのプロパティ名とFirestoreのフィールド名は以下の通り対応させる。
 
```
userName          → user_name
carryingLetterIds → carrying_letter_ids
letterId          → letter_id
toUser            → to_user
fromUser          → from_user
isSurvival        → is_survival
locationId        → location_id
encounterId       → encounter_id
userA             → userA
userB             → userB
fromNodeId        → from_node_id
toNodeId          → to_node_id
```
 
---

## コア機能（重要）

### すれ違い処理（RelayLetterUseCase）

処理内容:

1. Encounterの重複チェック（一定時間以内の同一ペアはスキップ）
2. 相手の手紙取得（is_survival=true かつ 相手が運搬中）
3. 自分がすでにtreeに含まれる手紙はスキップ
4. 自分の運搬リストに追加
5. location記録
6. tree更新（node / edge追加）
7. 自分が宛先なら is_survival=false に更新

### 投函処理
 
1. LETTERSに手紙を登録
2. 差出人をrootノードとしてtreeに登録
3. 投函位置をLOCATIONSに保存
4. 差出人端末からは本文を見えない状態にする（投函後に下書き削除）

---

## データ構造

```
letters     手紙本体
locations   中継履歴
encounters  すれ違い履歴
tree        表示用構造
```

---

## イベント処理

### UIイベント

```
UI → ViewModel → (UseCase / Repository)
```

---

### BLEイベント

```
BLE → UseCase → Repository
```

---

## ナビゲーション

ルール:

- ViewModelは遷移しない
- ViewModelはイベント発行のみ
- UIでnavControllerを使用

---

## 開発フロー（最重要）

すべての開発はこの手順で行う:

---

### 0. Issue / sub-issue 単位の進め方

親 issue に取り組むときは、最初に親 issue 用のブランチを作成する。

```
issue-番号-内容
```

例:
```
issue-14-project-foundation
```

sub-issue がある場合は、以下を必ず守る:

- sub-issue を 1 つずつ実装する
- sub-issue 1 つにつき 1 commit にまとめる
- 各 commit 後に push して確認できる状態にする
- すべての sub-issue 完了後、親 issue の完了条件を確認する
- 完了条件を満たしてから main 向け Pull Request を作成する

PR 作成前の確認:

```
./gradlew test
./gradlew :app:assembleDebug
```

---

### 1. ブランチ作成

```
feature/機能名
```

例:
```
feature/edit-letter
feature/relay-letter
```

---

### 2. 実装順序

必ずこの順番で書く

```
① ViewModel
② Repository
③ UseCase
④ UI
```

---

### 3. テスト作成

機能ごとにテストを書く

対象:
- UseCase（必須）
- ViewModel（推奨）

---

### 4. テスト実行

```
./gradlew test
```

または

Android Studio
→ Run test

---

### 5. 確認

- テストがすべて成功
- 手動でUI確認

---

### 6. コミット

```
git add .
git commit -m "feat: 機能名"
```

---

### 7. プッシュ

```
git push origin feature/機能名
```

---

### 8. マージ

- Pull Request作成
- レビュー
- mainにマージ

---

## テスト方針

---

### UseCaseテスト（必須）

例:

```kotlin
@Test
fun `すれ違い時に手紙がコピーされる`() {

    val useCase = RelayLetterUseCase(
        fakeLetterRepository,
        fakeLocationRepository
    )

    useCase.execute("A", "B")

    assert(コピーされている)
}
```

---

### ViewModelテスト

```kotlin
@Test
fun `ボタンクリックでイベントが発生する`() {
    viewModel.onClick()
    assert(event emitted)
}
```

---

### FakeRepository使用

テストでは必ずFakeを使う

例:
```
FakeLetterRepository
FakeLocationRepository
```

---

## 非同期処理ルール

- Coroutineを使用
- suspend関数で実装
- ViewModelはviewModelScope

---

## DI 方針

現時点では Hilt は導入しない。

理由:
- 依存関係の数がまだ少ない
- Repository / DataSource の実装が固まっていない
- まず手動の constructor injection で依存方向を明確にする

ルール:
- 依存関係の生成は `di/AppContainer.kt` に集約する
- ViewModel / UseCase / Repository は constructor injection で依存を受け取る
- UI から Repository / DataSource を直接生成しない
- 依存関係が増えて手動管理が複雑になったら Hilt 導入を検討する

---

## 命名規則

```
Screen         UI
ViewModel      状態管理
UseCase        ○○UseCase
Repository     ○○Repository
DataSource     ○○DataSource
```

---

## 禁止事項

- UIからRepository直接呼び出し
- ViewModelからDataSource呼び出し
- UseCaseにUI処理を書く
- ロジックをUIに書く

---

## 最重要ルール

```
UseCaseが処理の中心
```

---
