package com.example.eventra.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import com.example.eventra.R
import com.example.eventra.untils.SessionManager
import com.example.eventra.viewmodels.data.ApiResponseAuth
import com.example.eventra.viewmodels.data.ErrorData
import com.example.eventra.viewmodels.data.LoginRequest
import com.example.eventra.viewmodels.data.LoginResponse
import com.example.eventra.viewmodels.data.RegistrationRequest

import java.net.HttpURLConnection
import java.net.URL

class LoginViewModel(application: Application) : ViewModel() {
    private val _application = application
    private val server = application.getString(R.string.server)
    private val sessionManager = SessionManager(application)

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<ErrorData?>(null)
    val error: StateFlow<ErrorData?> = _error.asStateFlow()

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    sealed class LoginState {
        object Idle : LoginState()
        object Success : LoginState()
        data class Error(val message: String) : LoginState()
    }

    fun login(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            _loginState.value = LoginState.Error("Email e password sono obbligatori")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val success = performLogin(email, password)
                if (success) {
                    _loginState.value = LoginState.Success
                }
            } catch (e: Exception) {
                Log.e("LoginViewModel", "Errore durante il login", e)
                _loginState.value = LoginState.Error("Errore di connessione: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun performLogin(email: String, password: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("$server/api/utente/login")
                Log.d("LoginViewModel", "URL login: $url")

                val jsonBody = JSONObject().apply {
                    put("email", email)
                    put("password", password)
                }.toString()

                val requestBody = jsonBody.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

                val client = OkHttpClient()
                val request = Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build()

                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    Log.d("LoginViewModel", "Response: $responseBody")

                    responseBody?.let { body ->
                        val jsonResponse = JSONObject(body)
                        val success = jsonResponse.getBoolean("success")

                        if (success) {
                            val dataObject = jsonResponse.getJSONObject("data")
                            val token = dataObject.getString("token")
                            val refreshToken = dataObject.getString("refreshToken")
                            val userId = dataObject.getLong("userId")

                            sessionManager.saveUserSession(token, refreshToken, userId)
                            true
                        } else {
                            val message = jsonResponse.getString("message")
                            _loginState.value = LoginState.Error(message)
                            false
                        }
                    } ?: false
                } else {
                    when (response.code) {
                        HttpURLConnection.HTTP_UNAUTHORIZED -> {
                            _loginState.value = LoginState.Error("Credenziali non valide")
                        }
                        HttpURLConnection.HTTP_NOT_FOUND -> {
                            _loginState.value = LoginState.Error("Utente non trovato")
                        }
                        else -> {
                            _loginState.value = LoginState.Error("Errore del server: ${response.code}")
                        }
                    }
                    false
                }
            } catch (e: Exception) {
                Log.e("LoginViewModel", "Errore nella richiesta di login", e)
                _loginState.value = LoginState.Error("Errore di connessione")
                false
            }
        }
    }

    fun register(nome: String, cognome: String, email: String, password: String, telefono: String = "") {
        if (nome.isEmpty() || cognome.isEmpty() || email.isEmpty() || password.isEmpty()) {
            _loginState.value = LoginState.Error("Tutti i campi obbligatori devono essere compilati")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val success = performRegistration(nome, cognome, email, password, telefono)
                if (success) {
                    // Dopo la registrazione, effettua automaticamente il login
                    performLogin(email, password)
                }
            } catch (e: Exception) {
                Log.e("LoginViewModel", "Errore durante la registrazione", e)
                _loginState.value = LoginState.Error("Errore di connessione: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun performRegistration(
        nome: String,
        cognome: String,
        email: String,
        password: String,
        telefono: String
    ): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("$server/api/utente/register")
                Log.d("LoginViewModel", "URL registrazione: $url")

                val jsonBody = JSONObject().apply {
                    put("nome", nome)
                    put("cognome", cognome)
                    put("email", email)
                    put("password", password)
                    if (telefono.isNotEmpty()) {
                        put("numerotelefono", telefono)
                    }
                }.toString()

                val requestBody = jsonBody.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

                val client = OkHttpClient()
                val request = Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build()

                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    Log.d("LoginViewModel", "Response registrazione: $responseBody")

                    responseBody?.let { body ->
                        val jsonResponse = JSONObject(body)
                        val success = jsonResponse.getBoolean("success")

                        if (!success) {
                            val message = jsonResponse.getString("message")
                            _loginState.value = LoginState.Error(message)
                        }
                        success
                    } ?: false
                } else {
                    when (response.code) {
                        HttpURLConnection.HTTP_BAD_REQUEST -> {
                            _loginState.value = LoginState.Error("Email già in uso o dati non validi")
                        }
                        else -> {
                            _loginState.value = LoginState.Error("Errore del server: ${response.code}")
                        }
                    }
                    false
                }
            } catch (e: Exception) {
                Log.e("LoginViewModel", "Errore nella richiesta di registrazione", e)
                _loginState.value = LoginState.Error("Errore di connessione")
                false
            }
        }
    }

    fun isUserLoggedIn(): Boolean = sessionManager.isLoggedIn()

    fun clearError() {
        _error.value = null
        _loginState.value = LoginState.Idle
    }
}
