package com.example.eventra.untils

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SessionManager(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "eventra_session",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveUserSession(token: String, refreshToken: String, userId: Long) {
        with(sharedPreferences.edit()) {
            putString("jwt_token", token)
            putString("refresh_token", refreshToken)
            putLong("user_id", userId)
            putBoolean("is_logged_in", true)
            apply()
        }
    }

    fun getJwtToken(): String? = sharedPreferences.getString("jwt_token", null)

    fun getRefreshToken(): String? = sharedPreferences.getString("refresh_token", null)

    fun getUserId(): Long = sharedPreferences.getLong("user_id", -1)

    fun isLoggedIn(): Boolean = sharedPreferences.getBoolean("is_logged_in", false)

    fun clearSession() {
        with(sharedPreferences.edit()) {
            clear()
            apply()
        }
    }
}
