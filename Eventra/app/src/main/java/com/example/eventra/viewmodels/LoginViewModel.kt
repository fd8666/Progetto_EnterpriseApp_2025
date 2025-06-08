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
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import com.example.eventra.R
import com.example.eventra.untils.SessionManager
import java.net.HttpURLConnection
import java.util.concurrent.TimeUnit

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val _application = application
    private val server = application.getString(R.string.server)
    private val sessionManager = SessionManager(application)

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    sealed class LoginState {
        object Idle : LoginState()
        object Success : LoginState()
        data class Error(val message: String) : LoginState()
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _loginState.value = LoginState.Error("Email e password sono obbligatori")
            return
        }

        if (!isValidEmail(email)) {
            _loginState.value = LoginState.Error("Inserisci un'email valida")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _loginState.value = LoginState.Idle

            try {
                val success = performLogin(email.trim(), password)
                if (success) {
                    _loginState.value = LoginState.Success
                }
            } catch (e: Exception) {
                Log.e("LoginViewModel", "Errore durante il login", e)
                _loginState.value = LoginState.Error("Errore di connessione. Riprova più tardi.")
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun performLogin(email: String, password: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val url = "$server/api/utente/login"
                Log.d("LoginViewModel", "URL login: $url")

                val jsonBody = JSONObject().apply {
                    put("email", email)
                    put("password", password)
                }.toString()

                Log.d("LoginViewModel", "Request body: $jsonBody")

                val requestBody = jsonBody.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

                val request = Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "application/json")
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                Log.d("LoginViewModel", "Response code: ${response.code}")
                Log.d("LoginViewModel", "Response body: $responseBody")

                if (response.isSuccessful && responseBody != null) {
                    val jsonResponse = JSONObject(responseBody)
                    val success = jsonResponse.optBoolean("success", false)

                    if (success) {
                        val dataObject = jsonResponse.optJSONObject("data")
                        if (dataObject != null) {
                            val token = dataObject.optString("token", "")
                            val refreshToken = dataObject.optString("refreshToken", "")
                            // CORRETTO: Usa "utente" dal backend
                            val userId = dataObject.optLong("utente", -1)

                            if (token.isNotEmpty() && userId != -1L) {
                                sessionManager.saveUserSession(token, refreshToken, userId)
                                Log.d("LoginViewModel", "Login successful, session saved")
                                true
                            } else {
                                _loginState.value = LoginState.Error("Dati di risposta non validi")
                                false
                            }
                        } else {
                            _loginState.value = LoginState.Error("Risposta del server non valida")
                            false
                        }
                    } else {
                        val message = jsonResponse.optString("message", "Credenziali non valide")
                        _loginState.value = LoginState.Error(message)
                        false
                    }
                } else {
                    handleErrorResponse(response.code, responseBody)
                    false
                }
            } catch (e: Exception) {
                Log.e("LoginViewModel", "Errore nella richiesta di login", e)
                _loginState.value = LoginState.Error("Errore di connessione. Verifica la tua rete.")
                false
            }
        }
    }

    fun register(nome: String, cognome: String, email: String, password: String, telefono: String = "") {
        if (nome.isBlank() || cognome.isBlank() || email.isBlank() || password.isBlank()) {
            _loginState.value = LoginState.Error("Tutti i campi obbligatori devono essere compilati")
            return
        }

        if (nome.length < 2 || nome.length > 12) {
            _loginState.value = LoginState.Error("Il nome deve essere tra 2 e 12 caratteri")
            return
        }

        if (cognome.length < 2 || cognome.length > 12) {
            _loginState.value = LoginState.Error("Il cognome deve essere tra 2 e 12 caratteri")
            return
        }

        if (!isValidEmail(email)) {
            _loginState.value = LoginState.Error("Inserisci un'email valida")
            return
        }

        if (!isValidPassword(password)) {
            _loginState.value = LoginState.Error("La password deve contenere almeno una maiuscola, una minuscola, un numero e un carattere speciale (@\$!%*?&)")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _loginState.value = LoginState.Idle

            try {
                val success = performRegistration(nome.trim(), cognome.trim(), email.trim(), password, telefono.trim())
                if (success) {
                    _loginState.value = LoginState.Success
                }
            } catch (e: Exception) {
                Log.e("LoginViewModel", "Errore durante la registrazione", e)
                _loginState.value = LoginState.Error("Errore di connessione. Riprova più tardi.")
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
                val url = "$server/api/utente/register"
                Log.d("LoginViewModel", "URL registrazione: $url")

                val jsonBody = JSONObject().apply {
                    put("nome", nome)
                    put("cognome", cognome)
                    put("email", email)
                    put("password", password)
                    put("numerotelefono", telefono.ifEmpty { "1234567890" })
                }.toString()

                Log.d("LoginViewModel", "Request body: $jsonBody")

                val requestBody = jsonBody.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

                val request = Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "application/json")
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                Log.d("LoginViewModel", "Response code: ${response.code}")
                Log.d("LoginViewModel", "Response body: $responseBody")

                if (response.isSuccessful && responseBody != null) {
                    val jsonResponse = JSONObject(responseBody)
                    val success = jsonResponse.optBoolean("success", false)

                    if (success) {
                        val dataObject = jsonResponse.optJSONObject("data")
                        if (dataObject != null) {
                            val token = dataObject.optString("token", "")
                            val refreshToken = dataObject.optString("refreshToken", "")
                            // CORRETTO: Cerca prima "utente", poi "userId" come fallback
                            val userId = dataObject.optLong("utente", -1).let {
                                if (it == -1L) dataObject.optLong("userId", -1) else it
                            }

                            if (token.isNotEmpty() && userId != -1L) {
                                sessionManager.saveUserSession(token, refreshToken, userId)
                                Log.d("LoginViewModel", "Registration successful, session saved")
                                true
                            } else {
                                _loginState.value = LoginState.Error("Dati di risposta non validi")
                                false
                            }
                        } else {
                            _loginState.value = LoginState.Error("Risposta del server non valida")
                            false
                        }
                    } else {
                        val message = jsonResponse.optString("message", "Errore durante la registrazione")
                        _loginState.value = LoginState.Error(message)
                        false
                    }
                } else {
                    handleErrorResponse(response.code, responseBody)
                    false
                }
            } catch (e: Exception) {
                Log.e("LoginViewModel", "Errore nella richiesta di registrazione", e)
                _loginState.value = LoginState.Error("Errore di connessione. Verifica la tua rete.")
                false
            }
        }
    }

    private fun handleErrorResponse(code: Int, responseBody: String?) {
        try {
            if (responseBody != null) {
                val jsonResponse = JSONObject(responseBody)
                val message = jsonResponse.optString("message", "")
                if (message.isNotEmpty()) {
                    _loginState.value = LoginState.Error(message)
                    return
                }
            }
        } catch (e: Exception) {
            Log.e("LoginViewModel", "Error parsing error response", e)
        }

        when (code) {
            HttpURLConnection.HTTP_UNAUTHORIZED -> {
                _loginState.value = LoginState.Error("Email o password non corretti")
            }
            HttpURLConnection.HTTP_NOT_FOUND -> {
                _loginState.value = LoginState.Error("Utente non trovato")
            }
            HttpURLConnection.HTTP_BAD_REQUEST -> {
                _loginState.value = LoginState.Error("Email già in uso o dati non validi")
            }
            HttpURLConnection.HTTP_CONFLICT -> {
                _loginState.value = LoginState.Error("Un utente con questa email esiste già")
            }
            HttpURLConnection.HTTP_INTERNAL_ERROR -> {
                _loginState.value = LoginState.Error("Errore del server. Riprova più tardi.")
            }
            else -> {
                _loginState.value = LoginState.Error("Errore di connessione (Codice: $code)")
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isValidPassword(password: String): Boolean {
        val pattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{6,}$"
        return password.matches(pattern.toRegex())
    }

    fun isUserLoggedIn(): Boolean = sessionManager.isLoggedIn()

    fun clearError() {
        _loginState.value = LoginState.Idle
    }

    fun logout() {
        sessionManager.clearSession()
        _loginState.value = LoginState.Idle
    }

    fun refreshToken() {
        val refreshToken = sessionManager.getRefreshToken()
        if (refreshToken.isNullOrEmpty()) {
            logout()
            return
        }

        viewModelScope.launch {
            try {
                val success = performTokenRefresh(refreshToken)
                if (!success) {
                    logout()
                }
            } catch (e: Exception) {
                Log.e("LoginViewModel", "Errore refresh token", e)
                logout()
            }
        }
    }

    private suspend fun performTokenRefresh(refreshToken: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val url = "$server/api/utente/refresh?refreshtoken=$refreshToken"

                val request = Request.Builder()
                    .url(url)
                    .post("".toRequestBody())
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "application/json")
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                if (response.isSuccessful && responseBody != null) {
                    val jsonResponse = JSONObject(responseBody)
                    val newToken = jsonResponse.optString("token", "")
                    val newRefreshToken = jsonResponse.optString("refreshToken", refreshToken)
                    val userId = sessionManager.getUserId()

                    if (newToken.isNotEmpty() && userId != -1L) {
                        sessionManager.saveUserSession(newToken, newRefreshToken, userId)
                        true
                    } else {
                        false
                    }
                } else {
                    false
                }
            } catch (e: Exception) {
                Log.e("LoginViewModel", "Errore refresh token", e)
                false
            }
        }
    }
}