package com.example.eventra.untils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.eventra.viewmodels.data.AppTheme

class SessionManager(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences: SharedPreferences = try {
        EncryptedSharedPreferences.create(
            context,
            "eventra_session",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        Log.e("SessionManager", "Error creating encrypted preferences, falling back to regular", e)
        context.getSharedPreferences("eventra_session_fallback", Context.MODE_PRIVATE)
    }

    fun saveUserSession(token: String, refreshToken: String, userId: Long) {
        with(sharedPreferences.edit()) {
            putString("jwt_token", token)
            putString("refresh_token", refreshToken)
            putLong("user_id", userId)
            putBoolean("is_logged_in", true)
            putLong("login_timestamp", System.currentTimeMillis())
            apply()
        }
        Log.d("SessionManager", "Session saved for user: $userId")
    }

    fun getJwtToken(): String? {
        val token = sharedPreferences.getString("jwt_token", null)
        Log.d("SessionManager", "Retrieved JWT token: ${if (token != null) "presente" else "null"}")
        return token
    }

    fun getRefreshToken(): String? {
        val refreshToken = sharedPreferences.getString("refresh_token", null)
        Log.d("SessionManager", "Retrieved refresh token: ${if (refreshToken != null) "presente" else "null"}")
        return refreshToken
    }

    fun getUserId(): Long {
        val userId = sharedPreferences.getLong("user_id", -1)
        Log.d("SessionManager", "Retrieved user ID: $userId")
        return userId
    }

    fun isLoggedIn(): Boolean {
        val isLoggedIn = sharedPreferences.getBoolean("is_logged_in", false)
        val hasToken = getJwtToken() != null
        val hasUserId = getUserId() != -1L

        val result = isLoggedIn && hasToken && hasUserId
        Log.d("SessionManager", "Is logged in: $result (flag: $isLoggedIn, hasToken: $hasToken, hasUserId: $hasUserId)")
        return result
    }

    fun getLoginTimestamp(): Long {
        return sharedPreferences.getLong("login_timestamp", 0)
    }

    fun clearSession() {
        Log.d("SessionManager", "Clearing session")
        with(sharedPreferences.edit()) {
            clear()
            apply()
        }
    }

    fun getAuthorizationHeader(): String? {
        val token = getJwtToken()
        return if (token != null) "Bearer $token" else null
    }

    fun isTokenExpired(): Boolean {
        val loginTime = getLoginTimestamp()
        val currentTime = System.currentTimeMillis()
        val oneHour = 60 * 60 * 1000

        return (currentTime - loginTime) > oneHour
    }

    // GESTIONE TEMA - CORRETTA
    fun saveTheme(theme: AppTheme) {
        with(sharedPreferences.edit()) {
            putString("app_theme", theme.name)
            apply()
        }
        Log.d("SessionManager", "Theme saved: ${theme.name}")
    }

    fun getTheme(): AppTheme {
        val themeName = sharedPreferences.getString("app_theme", AppTheme.SYSTEM.name)
        return try {
            AppTheme.valueOf(themeName ?: AppTheme.SYSTEM.name)
        } catch (e: IllegalArgumentException) {
            Log.w("SessionManager", "Invalid theme name: $themeName, using SYSTEM")
            AppTheme.SYSTEM
        }
    }
}