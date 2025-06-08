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
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import com.example.eventra.R
import com.example.eventra.untils.SessionManager
import com.example.eventra.viewmodels.data.*
import java.net.HttpURLConnection
import java.util.concurrent.TimeUnit

class OrderHistoryViewModel(application: Application) : AndroidViewModel(application) {
    private val _application = application
    private val server = application.getString(R.string.server)
    private val sessionManager = SessionManager(application)

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _biglietti = MutableStateFlow<List<BigliettoData>>(emptyList())
    val biglietti: StateFlow<List<BigliettoData>> = _biglietti.asStateFlow()

    private val _errorState = MutableStateFlow<String?>(null)
    val errorState: StateFlow<String?> = _errorState.asStateFlow()

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    fun loadUserTickets() {
        val userId = sessionManager.getUserId()
        if (userId == -1L) {
            _errorState.value = "Utente non autenticato"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorState.value = null

            try {
                val tickets = fetchUserTickets(userId)
                _biglietti.value = tickets
                Log.d("OrderHistoryViewModel", "Loaded ${tickets.size} tickets")
            } catch (e: Exception) {
                Log.e("OrderHistoryViewModel", "Errore nel caricamento biglietti", e)
                _errorState.value = "Errore nel caricamento dello storico"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun fetchUserTickets(userId: Long): List<BigliettoData> {
        return withContext(Dispatchers.IO) {
            try {
                val token = sessionManager.getJwtToken()
                if (token.isNullOrEmpty()) {
                    Log.e("OrderHistoryViewModel", "Token JWT non disponibile")
                    return@withContext emptyList()
                }

                val url = "$server/api/biglietto/utente/$userId"

                Log.d("OrderHistoryViewModel", "Calling URL: $url")
                Log.d("OrderHistoryViewModel", "User ID: $userId")
                Log.d("OrderHistoryViewModel", "Token present: ${token.isNotEmpty()}")

                val request = Request.Builder()
                    .url(url)
                    .get()
                    .addHeader("Authorization", "Bearer $token")
                    .addHeader("Accept", "application/json")
                    .build()

                val response = client.newCall(request).execute()

                Log.d("OrderHistoryViewModel", "Response code: ${response.code}")
                Log.d("OrderHistoryViewModel", "Response successful: ${response.isSuccessful}")

                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    Log.d("OrderHistoryViewModel", "Response body: $responseBody")

                    if (responseBody != null) {
                        parseTicketsResponse(responseBody)
                    } else {
                        Log.e("OrderHistoryViewModel", "Response body is null")
                        emptyList()
                    }
                } else {
                    when (response.code) {
                        HttpURLConnection.HTTP_UNAUTHORIZED -> {
                            Log.e("OrderHistoryViewModel", "Token scaduto o non valido")
                            sessionManager.clearSession()
                        }
                        HttpURLConnection.HTTP_FORBIDDEN -> {
                            Log.e("OrderHistoryViewModel", "Accesso negato")
                        }
                        HttpURLConnection.HTTP_NOT_FOUND -> {
                            Log.e("OrderHistoryViewModel", "Endpoint non trovato")
                        }
                    }
                    Log.e("OrderHistoryViewModel", "Errore HTTP: ${response.code}")
                    emptyList()
                }
            } catch (e: Exception) {
                Log.e("OrderHistoryViewModel", "Errore nel fetch biglietti", e)
                emptyList()
            }
        }
    }

    private fun parseTicketsResponse(responseBody: String): List<BigliettoData> {
        return try {
            val tickets = mutableListOf<BigliettoData>()

            // Prova prima il formato con wrapper "success" e "data"
            try {
                val jsonWrapper = JSONObject(responseBody)
                if (jsonWrapper.has("success") && jsonWrapper.getBoolean("success")) {
                    val dataArray = jsonWrapper.getJSONArray("data")
                    for (i in 0 until dataArray.length()) {
                        val ticketJson = dataArray.getJSONObject(i)
                        tickets.add(parseTicketFromJson(ticketJson))
                    }
                } else {
                    // Formato array diretto
                    val jsonArray = JSONArray(responseBody)
                    for (i in 0 until jsonArray.length()) {
                        val ticketJson = jsonArray.getJSONObject(i)
                        tickets.add(parseTicketFromJson(ticketJson))
                    }
                }
            } catch (e: Exception) {
                // Se fallisce, prova il formato array diretto
                val jsonArray = JSONArray(responseBody)
                for (i in 0 until jsonArray.length()) {
                    val ticketJson = jsonArray.getJSONObject(i)
                    tickets.add(parseTicketFromJson(ticketJson))
                }
            }

            Log.d("OrderHistoryViewModel", "Parsed ${tickets.size} tickets successfully")
            tickets
        } catch (e: Exception) {
            Log.e("OrderHistoryViewModel", "Errore nel parsing biglietti", e)
            emptyList()
        }
    }

    private fun parseTicketFromJson(ticketJson: JSONObject): BigliettoData {
        return BigliettoData(
            id = ticketJson.optLong("id", 0),
            nomeSpettatore = ticketJson.optString("nomeSpettatore", ""),
            cognomeSpettatore = ticketJson.optString("cognomeSpettatore", ""),
            prezzo = ticketJson.optDouble("prezzo", 0.0),
            eventoNome = ticketJson.optString("eventoNome", "Evento sconosciuto"),
            tipoPostoNome = ticketJson.optString("tipoPostoNome", "Posto standard"),
            dataEvento = ticketJson.optString("dataEvento", "Data non disponibile")
        )
    }

    fun clearError() {
        _errorState.value = null
    }

    fun retry() {
        clearError()
        loadUserTickets()
    }
}