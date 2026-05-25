package com.example.letterble.data.datasource.local

import android.content.Context

class UserLocalDataSource(
    context: Context
) {
    private val sharedPreferences = context.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE
    )

    fun saveCurrentUserName(userName: String) {
        sharedPreferences
            .edit()
            .putString(KEY_CURRENT_USER_NAME, userName)
            .apply()
    }

    fun getCurrentUserName(): String? {
        return sharedPreferences.getString(KEY_CURRENT_USER_NAME, null)
    }

    fun clearCurrentUserName() {
        sharedPreferences
            .edit()
            .remove(KEY_CURRENT_USER_NAME)
            .apply()
    }

    private companion object {
        const val PREFERENCES_NAME = "letter_ble_user_preferences"
        const val KEY_CURRENT_USER_NAME = "current_user_name"
    }
}