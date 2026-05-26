/**
 * 位置履歴データをアプリ側から扱いやすい形で提供する Repository。
 *
 * Firestore の LOCATIONS コレクションへの保存・取得は
 * LocationFirestoreDataSource に任せる。
 */
package com.example.letterble.data.repository

import com.example.letterble.data.datasource.firestore.LocationFirestoreDataSource
import com.example.letterble.domain.model.Location

/**
 * LOCATIONS コレクションを扱う Repository。
 *
 * 今の段階では DataSource の薄いラッパーとして、上位層に必要な関数名を提供する。
 */
class LocationRepository(
    private val locationFirestoreDataSource: LocationFirestoreDataSource = LocationFirestoreDataSource()
) {
    /**
     * 投函地点や中継地点の位置履歴を保存する。
     */
    suspend fun saveLocation(location: Location) {
        locationFirestoreDataSource.saveLocation(location)
    }

    /**
     * 指定した手紙IDに紐づく位置履歴を取得する。
     *
     * 経路表示や Tree 生成で使う。
     */
    suspend fun getLocationsByLetter(letterId: String): List<Location> {
        return locationFirestoreDataSource.getLocationsByLetter(letterId)
    }

    /**
     * 指定したユーザー名に紐づく位置履歴を取得する。
     *
     * 受信一覧や運搬一覧の下準備で使えるようにしておく。
     */
    suspend fun getLocationsByUser(userName: String): List<Location> {
        return locationFirestoreDataSource.getLocationsByUser(userName)
    }
}
