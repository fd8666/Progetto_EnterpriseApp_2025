package com.example.eventra.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import com.example.eventra.R
import com.example.eventra.untils.SessionManager
import com.example.eventra.viewmodels.data.*
import java.net.HttpURLConnection
import java.util.concurrent.TimeUnit

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val _application = application
    private val server = application.getString(R.string.server)
    private val sessionManager = SessionManager(application)

    private val _userData = MutableStateFlow<UtenteData?>(null)
    val userData: StateFlow<UtenteData?> = _userData.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    private val _currentTheme = MutableStateFlow(sessionManager.getTheme())
    val currentTheme: StateFlow<AppTheme> = _currentTheme.asStateFlow()

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    sealed class UpdateState {
        object Idle : UpdateState()
        object Loading : UpdateState()
        object Success : UpdateState()
        data class Error(val message: String) : UpdateState()
    }

    init {
        loadUserProfile()
        _currentTheme.value = sessionManager.getTheme()
    }

    fun loadUserProfile() {
        val userId = sessionManager.getUserId()
        if (userId == -1L) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val user = fetchUserProfile(userId)
                _userData.value = user
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Errore nel caricamento profilo", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun fetchUserProfile(userId: Long): UtenteData? {
        return withContext(Dispatchers.IO) {
            try {
                val token = sessionManager.getJwtToken() ?: return@withContext null
                val url = "$server/api/utente/$userId"

                val request = Request.Builder()
                    .url(url)
                    .get()
                    .addHeader("Authorization", "Bearer $token")
                    .addHeader("Accept", "application/json")
                    .build()

                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    responseBody?.let { body ->
                        parseUserResponse(body)
                    }
                } else {
                    if (response.code == HttpURLConnection.HTTP_UNAUTHORIZED) {
                        sessionManager.clearSession()
                    }
                    null
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Errore nel fetch profilo", e)
                null
            }
        }
    }

    private fun parseUserResponse(responseBody: String): UtenteData? {
        return try {
            val jsonResponse = JSONObject(responseBody)

            // Controlla se la risposta ha il formato con "success" e "data"
            if (jsonResponse.has("success") && jsonResponse.getBoolean("success")) {
                val userJson = jsonResponse.getJSONObject("data")
                UtenteData(
                    id = userJson.optLong("id"),
                    nome = userJson.optString("nome", ""),
                    cognome = userJson.optString("cognome", ""),
                    email = userJson.optString("email", ""),
                    numerotelefono = userJson.optString("numerotelefono", "")
                )
            } else {
                // Formato diretto
                UtenteData(
                    id = jsonResponse.optLong("id"),
                    nome = jsonResponse.optString("nome", ""),
                    cognome = jsonResponse.optString("cognome", ""),
                    email = jsonResponse.optString("email", ""),
                    numerotelefono = jsonResponse.optString("numerotelefono", "")
                )
            }
        } catch (e: Exception) {
            Log.e("ProfileViewModel", "Errore nel parsing profilo", e)
            null
        }
    }

    fun updateUserProfile(nome: String, cognome: String, telefono: String) {
        val userId = sessionManager.getUserId()
        if (userId == -1L) return

        viewModelScope.launch {
            _updateState.value = UpdateState.Loading
            try {
                val success = updateProfile(userId, nome, cognome, telefono)
                if (success) {
                    _updateState.value = UpdateState.Success
                    loadUserProfile()
                } else {
                    _updateState.value = UpdateState.Error("Errore nell'aggiornamento del profilo")
                }
            } catch (e: Exception) {
                _updateState.value = UpdateState.Error(e.message ?: "Errore sconosciuto")
            }
        }
    }

    private suspend fun updateProfile(userId: Long, nome: String, cognome: String, telefono: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val token = sessionManager.getJwtToken() ?: return@withContext false
                val url = "$server/api/utente/aggiorna/$userId"

                val jsonBody = JSONObject().apply {
                    put("nome", nome)
                    put("cognome", cognome)
                    put("numerotelefono", telefono)
                }

                val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())

                val request = Request.Builder()
                    .url(url)
                    .put(requestBody)
                    .addHeader("Authorization", "Bearer $token")
                    .addHeader("Content-Type", "application/json")
                    .build()

                val response = client.newCall(request).execute()
                response.isSuccessful
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Errore nell'aggiornamento profilo", e)
                false
            }
        }
    }

    fun updatePassword(oldPassword: String, newPassword: String) {
        val userId = sessionManager.getUserId()
        if (userId == -1L) return

        viewModelScope.launch {
            _updateState.value = UpdateState.Loading
            try {
                val success = changePassword(userId, oldPassword, newPassword)
                if (success) {
                    _updateState.value = UpdateState.Success
                } else {
                    _updateState.value = UpdateState.Error("Password attuale non corretta")
                }
            } catch (e: Exception) {
                _updateState.value = UpdateState.Error(e.message ?: "Errore sconosciuto")
            }
        }
    }

    private suspend fun changePassword(userId: Long, oldPassword: String, newPassword: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val token = sessionManager.getJwtToken() ?: return@withContext false
                val url = "$server/api/utente/cambiaPassword/$userId"

                val jsonBody = JSONObject().apply {
                    put("oldPassword", oldPassword)
                    put("newPassword", newPassword)
                }

                val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())

                val request = Request.Builder()
                    .url(url)
                    .put(requestBody)
                    .addHeader("Authorization", "Bearer $token")
                    .addHeader("Content-Type", "application/json")
                    .build()

                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    if (responseBody != null) {
                        val jsonResponse = JSONObject(responseBody)
                        jsonResponse.optBoolean("success", false)
                    } else {
                        true
                    }
                } else {
                    false
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Errore nel cambio password", e)
                false
            }
        }
    }

    fun setTheme(theme: AppTheme) {
        viewModelScope.launch {
            try {
                sessionManager.saveTheme(theme)
                _currentTheme.value = theme
                Log.d("ProfileViewModel", "Theme updated to: ${theme.name}")
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error updating theme", e)
            }
        }
    }

    fun clearUpdateState() {
        _updateState.value = UpdateState.Idle
    }
}