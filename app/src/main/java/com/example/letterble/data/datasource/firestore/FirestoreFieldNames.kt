/**
 * Firestore の collection 名と field 名をまとめるファイル。
 *
 * DataSource で "letter_id" などの文字列を直接書くと、タイプミスや変更漏れが起きやすい。
 * Firestore の名前を使うときは、このファイルの定数を参照する。
 */
package com.example.letterble.data.datasource.firestore

/**
 * Firestore の collection 名。
 *
 * 例: firestore.collection(FirestoreCollections.LETTERS)
 */
internal object FirestoreCollections {
    const val USERS = "USERS"
    const val LETTERS = "LETTERS"
    const val LOCATIONS = "LOCATIONS"
    const val ENCOUNTERS = "ENCOUNTERS"
}

/**
 * Firestore の field 名。
 *
 * Kotlin のプロパティ名は camelCase、Firestore の field 名は snake_case が多いため、
 * モデルごとに分けて対応関係を見やすくしている。
 */
internal object FirestoreFields {
    object User {
        const val USER_NAME = "user_name"
        const val CARRYING_LETTER_IDS = "carrying_letter_ids"
    }

    object Letter {
        const val LETTER_ID = "letter_id"
        const val TO_USER = "to_user"
        const val FROM_USER = "from_user"
        const val SENTENCE = "sentence"
        const val IS_SURVIVAL = "is_survival"
        const val TREE = "tree"
    }

    object Location {
        const val LOCATION_ID = "location_id"
        const val LETTER_ID = "letter_id"
        const val USER_NAME = "user_name"
        const val LATITUDE = "latitude"
        const val LONGITUDE = "longitude"
        const val TIMESTAMP = "timestamp"
    }

    object Encounter {
        const val ENCOUNTER_ID = "encounter_id"
        const val USER_A = "userA"
        const val USER_B = "userB"
        const val TIMESTAMP = "timestamp"
    }

    object Tree {
        const val NODES = "nodes"
        const val EDGES = "edges"
    }

    object Node {
        const val ID = "id"
        const val USER_NAME = "user_name"
        const val LATITUDE = "latitude"
        const val LONGITUDE = "longitude"
    }

    object Edge {
        const val FROM_NODE_ID = "from_node_id"
        const val TO_NODE_ID = "to_node_id"
    }
}
